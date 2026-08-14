// Scratch helper: read captcha code from Redis and log in to the admin API.
const net = require('net');
const http = require('http');

function redisGet(host, port, key) {
    return new Promise((resolve, reject) => {
        const sock = net.connect(port, host, () => {
            sock.write(`*2\r\n$3\r\nGET\r\n$${Buffer.byteLength(key)}\r\n${key}\r\n`);
        });
        let buf = Buffer.alloc(0);
        const timer = setTimeout(() => { sock.destroy(); reject(new Error('redis timeout')); }, 5000);
        sock.on('data', (d) => {
            buf = Buffer.concat([buf, d]);
            const text = buf.toString('utf8');
            const nl = text.indexOf('\r\n');
            if (nl < 0) return;
            const head = text.slice(0, nl);
            if (head.startsWith('$')) {
                const len = parseInt(head.slice(1), 10);
                if (len === -1) { clearTimeout(timer); sock.destroy(); resolve(null); return; }
                const total = nl + 2 + len + 2;
                if (buf.length >= total) {
                    clearTimeout(timer); sock.destroy();
                    resolve(text.slice(nl + 2, nl + 2 + len));
                }
            } else if (head.startsWith('+') || head.startsWith('-') || head.startsWith(':')) {
                clearTimeout(timer); sock.destroy();
                resolve(head.startsWith('-') ? 'ERR:' + head : head.slice(1));
            }
        });
        sock.on('error', (e) => { clearTimeout(timer); reject(e); });
    });
}

function redisKeys(host, port, pattern) {
    return new Promise((resolve, reject) => {
        const sock = net.connect(port, host, () => {
            sock.write(`*2\r\n$4\r\nKEYS\r\n$${Buffer.byteLength(pattern)}\r\n${pattern}\r\n`);
        });
        let buf = Buffer.alloc(0);
        const timer = setTimeout(() => { sock.destroy(); reject(new Error('redis timeout')); }, 5000);
        sock.on('data', (d) => {
            buf = Buffer.concat([buf, d]);
            const text = buf.toString('utf8');
            if (text.startsWith('*')) {
                const nl = text.indexOf('\r\n');
                const count = parseInt(text.slice(1, nl), 10);
                // parse bulk strings until we have enough
                let pos = nl + 2;
                const items = [];
                for (let i = 0; i < count; i++) {
                    const nl2 = text.indexOf('\r\n', pos);
                    if (nl2 < 0) return;
                    const len = parseInt(text.slice(pos + 1, nl2), 10);
                    const start = nl2 + 2;
                    const end = start + len;
                    if (text.length < end + 2) return;
                    items.push(text.slice(start, end));
                    pos = end + 2;
                }
                clearTimeout(timer); sock.destroy();
                resolve(items);
            }
        });
        sock.on('error', (e) => { clearTimeout(timer); reject(e); });
    });
}

function httpJson(method, path, body) {
    return new Promise((resolve, reject) => {
        const data = body ? JSON.stringify(body) : null;
        const req = http.request({
            host: '127.0.0.1', port: 8082, path, method,
            headers: data ? { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) } : {}
        }, (res) => {
            let buf = '';
            res.on('data', (d) => buf += d);
            res.on('end', () => {
                try { resolve(JSON.parse(buf)); } catch (e) { reject(new Error('bad json: ' + buf.slice(0, 200))); }
            });
        });
        req.on('error', reject);
        if (data) req.write(data);
        req.end();
    });
}

(async () => {
    const captcha = await httpJson('GET', '/api/system/captcha');
    const uuid = captcha.data.uuid;
    console.log('uuid:', uuid);
    const key = `like:captcha:key:127001:${uuid}`;
    let code = await redisGet('127.0.0.1', 6379, key);
    console.log('raw code from redis:', code);
    if (code) { try { code = JSON.parse(code); } catch (e) { /* keep as-is */ } }
    console.log('parsed code:', code);
    if (!code) {
        const keys = await redisKeys('127.0.0.1', 6379, 'like:captcha:key:*');
        console.log('all captcha keys:', JSON.stringify(keys));
        if (keys && keys.length) {
            code = await redisGet('127.0.0.1', 6379, keys[keys.length - 1]);
            console.log('code from last key:', code);
        }
    }
    if (!code) { console.log('no captcha code found'); process.exit(2); }
    const login = await httpJson('POST', '/api/system/login', { username: 'admin', password: '123456', code, uuid });
    console.log('login response:', JSON.stringify(login).slice(0, 300));
    if (login.data && login.data.token) {
        console.log('TOKEN=' + login.data.token);
    }
})().catch((e) => { console.error('ERR', e.message); process.exit(1); });
