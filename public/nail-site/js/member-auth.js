(function () {
  "use strict";

  const pendingPromptKey = "xiang-nail-pending-prompt";
  const apiBase = location.protocol === "file:"
    ? "http://127.0.0.1:8082/api/nail/auth"
    : `${location.protocol}//${location.hostname}:8082/api/nail/auth`;

  async function request(path, options = {}) {
    const response = await fetch(`${apiBase}${path}`, {
      ...options,
      credentials: "include",
      headers: {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(options.headers || {})
      }
    });
    const payload = await response.json().catch(() => null);
    if (!response.ok || !payload || payload.code !== 200) {
      throw new Error(payload?.msg || "暂时无法连接登录服务，请稍后再试");
    }
    return payload.data;
  }

  window.NailMemberAuth = {
    savePrompt(value) {
      const prompt = String(value || "").trim().slice(0, 1000);
      if (prompt) sessionStorage.setItem(pendingPromptKey, prompt);
    },
    takePrompt() {
      const prompt = sessionStorage.getItem(pendingPromptKey) || "";
      sessionStorage.removeItem(pendingPromptKey);
      return prompt;
    },
    peekPrompt() {
      return sessionStorage.getItem(pendingPromptKey) || "";
    },
    async session() {
      return request("/session");
    },
    async isLoggedIn() {
      try {
        const session = await request("/session");
        return session?.loggedIn === true;
      } catch {
        return false;
      }
    },
    async login(username, password, options = {}) {
      return request("/login", {
        method: "POST",
        body: JSON.stringify({
          username,
          password,
          role: options.role || "USER",
          code: options.code || "",
          uuid: options.uuid || ""
        })
      });
    },
    async captcha() {
      return request("/captcha");
    },
    async logout() {
      return request("/logout", { method: "POST" });
    }
  };
})();
