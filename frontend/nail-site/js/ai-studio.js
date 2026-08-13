document.addEventListener("DOMContentLoaded", () => {
  const apiMeta = document.querySelector('meta[name="nail-ai-api"]')?.content || "/api/nail/public";
  const searchParams = new URLSearchParams(location.search);
  const apiOverride = searchParams.get("api");
  const queryPrompt = searchParams.get("q")?.trim().slice(0, 1000) || "";
  const creationTicket = searchParams.get("creation_ticket") || "";
  const entryPrompt = queryPrompt || window.NailMemberAuth?.takePrompt?.() || "";
  const localApi = location.protocol === "file:" ? "http://127.0.0.1:8082/api/nail/public" : `${location.protocol}//${location.hostname}:8082${apiMeta}`;
  const apiBase = (apiOverride || (apiMeta.startsWith("http") ? apiMeta : localApi)).replace(/\/$/, "");
  const bridgeApi = location.protocol === "file:"
    ? "http://127.0.0.1:8082/api/nail/creation-bridge"
    : `${location.origin}/api/nail/creation-bridge`;
  const storageKey = "xiang-nail-ai-records-v2";
  const form = document.getElementById("nailAiForm");
  if (!form) return;
  const mastheadSession = document.getElementById("mastheadSession");

  const elements = {
    newPanel: document.getElementById("newDesignPanel"), detailPanel: document.getElementById("taskDetailPanel"),
    newDesign: document.getElementById("newDesign"), refreshRecords: document.getElementById("refreshRecords"), recordList: document.getElementById("recordList"),
    prompt: document.getElementById("publicPrompt"), promptCount: document.getElementById("promptCount"), generate: document.getElementById("publicGenerate"), generateMeta: document.getElementById("generateMeta"),
    parameterTrigger: document.getElementById("parameterTrigger"), parameterSummary: document.getElementById("parameterSummary"), parameterPanel: document.getElementById("parameterPanel"), parameterBackdrop: document.getElementById("parameterBackdrop"), parameterClose: document.getElementById("parameterClose"),
    upload: document.getElementById("publicUpload"), fileInput: document.getElementById("publicFile"), preview: document.getElementById("publicPreview"), previewImage: document.getElementById("publicPreviewImage"), fileName: document.getElementById("publicFileName"), remove: document.getElementById("publicRemove"),
    referenceStrategyField: document.getElementById("referenceStrategyField"), trendOptions: document.getElementById("trendOptions"), composerSummary: document.getElementById("composerSummary"),
    nailShape: document.getElementById("nailShape"), finish: document.getElementById("finish"), designStyle: document.getElementById("designStyle"), layoutStyle: document.getElementById("layoutStyle"), colorPalette: document.getElementById("colorPalette"), referenceStrategy: document.getElementById("referenceStrategy"), aspectRatio: document.getElementById("aspectRatio"), resolution: document.getElementById("resolution"), outputCount: document.getElementById("outputCount"),
    taskBackToCreate: document.getElementById("taskBackToCreate"), taskEyebrow: document.getElementById("taskEyebrow"), taskTitle: document.getElementById("taskTitle"), taskMeta: document.getElementById("taskMeta"), status: document.getElementById("publicStatus"), taskCreateTime: document.getElementById("taskCreateTime"), taskPrompt: document.getElementById("taskPrompt"), taskSpecTags: document.getElementById("taskSpecTags"), taskReference: document.getElementById("taskReference"), taskResultCount: document.getElementById("taskResultCount"), taskBody: document.getElementById("publicTaskBody"),
    iterationForm: document.getElementById("iterationForm"), iterationPrompt: document.getElementById("iterationPrompt"), iterationGenerate: document.getElementById("iterationGenerate"), iterationNew: document.getElementById("iterationNew")
  };
  const parameterModalTargets = [document.querySelector(".site-header"), document.querySelector(".atelier-masthead"), document.querySelector(".ai-records"), document.querySelector(".ai-stage")].filter(Boolean);

  const labels = {
    shape: { SHORT_ALMOND: "短杏仁", SHORT_SQUOVAL: "短方圆", ALMOND: "杏仁", SQUARE: "方形", COFFIN: "芭蕾" },
    finish: { VELVET_CAT_EYE: "丝绒猫眼", JELLY: "果冻透色", CHROME: "镜面铬光", MICRO_FRENCH: "微法式", AURA: "晕染光圈", SCULPTED_GEL: "立体凝胶", GLOSSY_GEL: "高亮凝胶" },
    style: { QUIET_LUXURY: "克制高级", KOREAN_CLEAR: "韩系清透", RUNWAY: "秀场前卫", FUTURISTIC: "未来机能", ROMANTIC: "细腻浪漫", SWEET_COOL: "甜酷混搭" },
    layout: { UNIFIED: "十指统一", TWO_ACCENTS: "两枚重点甲", MICRO_FRENCH_LAYOUT: "微法式节奏", MISMATCHED: "错落混搭" },
    reference: { REINTERPRET: "提取设计语言", KEEP_PALETTE: "保留配色", KEEP_LAYOUT: "保留布局", KEEP_TEXTURE: "保留材质" },
    status: { QUEUED: "等待生成", RUNNING: "正在生成", SUCCEEDED: "生成完成", PARTIAL_SUCCEEDED: "部分完成", FAILED: "生成失败" }
  };

  let records = loadRecords();
  let creativeMode = "ON_HAND";
  let trendPreset = "ROSE_VELVET";
  let referenceAssetId;
  let currentTask;
  let currentAccess;
  let objectUrl;
  let pollTimer;
  let submitting = false;
  const customSelects = [];

  function syncCustomSelects() {
    customSelects.forEach(({ select, button, options }) => {
      button.querySelector("b").textContent = select.selectedOptions[0]?.textContent || "请选择";
      options.forEach(option => {
        const active = option.dataset.value === select.value;
        option.classList.toggle("is-selected", active);
        option.setAttribute("aria-selected", String(active));
      });
    });
  }

  function closeCustomSelects(except) {
    customSelects.forEach(item => {
      if (item === except) return;
      item.panel.hidden = true; item.button.setAttribute("aria-expanded", "false"); item.root.classList.remove("is-open");
    });
  }

  function enhanceInspectorSelects() {
    elements.parameterPanel.querySelectorAll(".inspector-fields select").forEach(select => {
      const root = document.createElement("div"); root.className = "atelier-select";
      const button = document.createElement("button"); button.type = "button"; button.className = "atelier-select-trigger";
      button.setAttribute("aria-haspopup", "listbox"); button.setAttribute("aria-expanded", "false");
      button.innerHTML = '<b></b><span aria-hidden="true"><i class="fa-solid fa-chevron-down"></i></span>';
      const panel = document.createElement("div"); panel.className = "atelier-select-menu"; panel.setAttribute("role", "listbox"); panel.hidden = true;
      const options = [...select.options].map(nativeOption => {
        const option = document.createElement("button"); option.type = "button"; option.className = "atelier-select-option";
        option.dataset.value = nativeOption.value; option.setAttribute("role", "option"); option.tabIndex = -1;
        const label = document.createElement("span"); label.textContent = nativeOption.textContent;
        const check = document.createElement("i"); check.className = "fa-solid fa-check"; check.setAttribute("aria-hidden", "true");
        option.append(label, check);
        option.addEventListener("click", () => {
          select.value = nativeOption.value; select.dispatchEvent(new Event("change", { bubbles: true }));
          panel.hidden = true; button.setAttribute("aria-expanded", "false"); root.classList.remove("is-open"); button.focus();
        });
        panel.append(option); return option;
      });
      select.classList.add("atelier-select-source"); select.tabIndex = -1; select.setAttribute("aria-hidden", "true");
      select.parentNode.insertBefore(root, select); root.append(select, button, panel);
      const item = { root, select, button, panel, options }; customSelects.push(item);
      button.addEventListener("click", () => {
        const willOpen = panel.hidden; closeCustomSelects(item); panel.hidden = !willOpen;
        button.setAttribute("aria-expanded", String(willOpen)); root.classList.toggle("is-open", willOpen);
        if (willOpen) options.find(option => option.dataset.value === select.value)?.focus();
      });
      root.addEventListener("keydown", event => {
        const activeIndex = options.indexOf(document.activeElement);
        if (event.key === "Escape") { event.preventDefault(); event.stopPropagation(); panel.hidden = true; button.setAttribute("aria-expanded", "false"); root.classList.remove("is-open"); button.focus(); }
        if (["ArrowDown", "ArrowUp"].includes(event.key) && !panel.hidden) {
          event.preventDefault(); const offset = event.key === "ArrowDown" ? 1 : -1;
          options[(activeIndex + offset + options.length) % options.length].focus();
        }
      });
    });
    document.addEventListener("click", event => { if (!event.target.closest(".atelier-select")) closeCustomSelects(); });
    syncCustomSelects();
  }

  function loadRecords() {
    try {
      const value = JSON.parse(localStorage.getItem(storageKey) || "[]");
      return Array.isArray(value) ? value.filter(item => item?.id && item?.accessToken).slice(0, 20) : [];
    } catch { return []; }
  }
  function persistRecords() { localStorage.setItem(storageKey, JSON.stringify(records.slice(0, 20))); }
  function safeUrl(value) {
    try { const url = new URL(value, location.href); return ["http:", "https:"].includes(url.protocol) ? url.href : ""; } catch { return ""; }
  }
  async function request(path, options = {}) {
    const response = await fetch(`${apiBase}${path}`, options);
    const payload = await response.json().catch(() => ({}));
    if (!response.ok || payload.code !== 200) throw new Error(payload.msg || `请求失败（${response.status}）`);
    return payload.data;
  }
  function statusClass(value) { return String(value || "").toLowerCase().replaceAll("_", "-"); }
  function updateRecord(task, access) {
    const existing = records.find(item => item.id === task.id);
    const next = {
      id: task.id, accessToken: access.accessToken, title: task.title || task.prompt || `设计记录 #${task.id}`,
      createTime: task.createTime || new Date().toLocaleString(), status: task.status,
      coverUrl: task.results?.[0]?.url || existing?.coverUrl || "", resultCount: task.results?.length || 0
    };
    records = [next, ...records.filter(item => item.id !== task.id)].slice(0, 20);
    persistRecords(); renderRecords();
  }
  function renderRecords() {
    elements.recordList.replaceChildren();
    if (!records.length) {
      const empty = document.createElement("div"); empty.className = "record-empty";
      const title = document.createElement("b"); title.textContent = "从第一套灵感开始";
      const note = document.createElement("small"); note.textContent = "生成后的设计会自动出现在这里";
      empty.append(title, note); elements.recordList.append(empty); return;
    }
    records.forEach(record => {
      const button = document.createElement("button"); button.type = "button"; button.className = `record-item${currentTask?.id === record.id ? " is-active" : ""}`;
      const cover = document.createElement("span"); cover.className = "record-cover";
      const coverUrl = safeUrl(record.coverUrl);
      if (coverUrl) { const image = document.createElement("img"); image.src = coverUrl; image.alt = record.title; cover.append(image); } else cover.textContent = "甲";
      const copy = document.createElement("span"); copy.className = "record-copy";
      const title = document.createElement("b"); title.textContent = record.title;
      const meta = document.createElement("small"); meta.textContent = `${record.createTime || ""} / ${record.resultCount || 0} 张方案`;
      const dot = document.createElement("i"); dot.className = `record-status ${statusClass(record.status)}`;
      copy.append(title, meta); button.append(cover, copy, dot);
      button.addEventListener("click", () => openRecord(record)); elements.recordList.append(button);
    });
  }
  function updateSummary() {
    elements.composerSummary.textContent = `${labels.shape[elements.nailShape.value]} / ${labels.finish[elements.finish.value]} / ${elements.aspectRatio.value}`;
    elements.generateMeta.textContent = `${elements.resolution.value} / ${elements.outputCount.value} 张`;
    elements.parameterSummary.textContent = `${elements.aspectRatio.value} / ${elements.resolution.value} / ${elements.outputCount.value} 张`;
    ["aspectRatio", "resolution", "outputCount"].forEach(target => {
      const field = elements[target];
      document.querySelectorAll(`[data-setting-target="${target}"]`).forEach(button => {
        const active = button.dataset.settingValue === field.value;
        button.classList.toggle("is-active", active);
        button.setAttribute("aria-pressed", String(active));
      });
    });
    syncCustomSelects();
  }
  function collectPayload(promptValue, existingReferenceId) {
    const activeReferenceId = existingReferenceId || referenceAssetId;
    return {
      taskType: activeReferenceId ? "IMAGE_TO_IMAGE" : "TEXT_TO_IMAGE", prompt: promptValue.trim(), creativeMode,
      nailShape: elements.nailShape.value, finish: elements.finish.value, designStyle: elements.designStyle.value,
      layoutStyle: elements.layoutStyle.value, trendPreset, referenceStrategy: elements.referenceStrategy.value,
      colorPalette: elements.colorPalette.value.trim(), aspectRatio: elements.aspectRatio.value,
      resolution: elements.resolution.value, outputCount: Number(elements.outputCount.value),
      referenceAssetId: activeReferenceId || undefined
    };
  }
  function applyTaskSpec(task) {
    const spec = task.designSpec || {};
    creativeMode = spec.creativeMode || task.creativeMode || "ON_HAND";
    document.querySelectorAll("[data-creative-mode]").forEach(button => button.classList.toggle("is-active", button.dataset.creativeMode === creativeMode));
    [[elements.nailShape, spec.nailShape], [elements.finish, spec.finish], [elements.designStyle, spec.designStyle], [elements.layoutStyle, spec.layoutStyle], [elements.referenceStrategy, spec.referenceStrategy], [elements.aspectRatio, task.aspectRatio], [elements.resolution, task.resolution], [elements.outputCount, String(task.outputCount)]].forEach(([field, value]) => { if (value) field.value = value; });
    elements.colorPalette.value = spec.colorPalette || ""; trendPreset = spec.trendPreset || "CUSTOM";
    elements.trendOptions.querySelectorAll("button").forEach(button => button.classList.toggle("is-active", button.dataset.value === trendPreset));
    elements.referenceStrategyField.hidden = !task.referenceAssetId; updateSummary();
  }
  function showNewDesign() {
    currentTask = undefined; currentAccess = undefined;
    setParametersOpen(false);
    elements.detailPanel.hidden = true; elements.newPanel.hidden = false; elements.prompt.focus(); renderRecords();
  }
  function setParametersOpen(open, restoreFocus = false) {
    elements.parameterPanel.hidden = !open; elements.parameterBackdrop.hidden = !open;
    elements.parameterTrigger.setAttribute("aria-expanded", String(open));
    document.body.classList.toggle("has-parameter-popover", open);
    parameterModalTargets.forEach(target => {
      target.toggleAttribute("inert", open);
      if (open) target.setAttribute("aria-hidden", "true");
      else target.removeAttribute("aria-hidden");
    });
    if (open) elements.parameterClose.focus();
    else if (restoreFocus) elements.parameterTrigger.focus();
  }
  function showLoading(message = "正在研色、构图与模拟材质") {
    elements.taskBody.innerHTML = '<div class="public-loading"><div class="public-loading-mark"><i></i><i></i><i></i><i></i><i></i></div><b></b><p>云端任务已创建，离开页面不会中断任务。</p></div>';
    elements.taskBody.querySelector("b").textContent = message;
  }
  function showError(message) {
    elements.taskBody.replaceChildren();
    const box = document.createElement("div"); box.className = "public-error";
    const title = document.createElement("b"); title.textContent = "这次没有生成成功";
    const note = document.createElement("p"); note.textContent = message || "请调整设计意图后重试";
    box.append(title, note); elements.taskBody.append(box);
  }
  function renderTask(task, access) {
    currentTask = task; currentAccess = access;
    setParametersOpen(false);
    elements.newPanel.hidden = true; elements.detailPanel.hidden = false;
    const createdAt = new Date(task.createTime || Date.now());
    elements.detailPanel.dataset.date = Number.isNaN(createdAt.getTime()) ? "创作记录" : `${createdAt.getMonth() + 1}月${createdAt.getDate()}日`;
    if (elements.taskEyebrow) elements.taskEyebrow.textContent = `设计档案 / ${task.id}`;
    elements.taskTitle.textContent = task.title || `设计记录 #${task.id}`;
    elements.taskMeta.textContent = `${task.creativeMode === "DESIGN_BOARD" ? "明档设计稿" : "真人上手效果"} / ${task.aspectRatio} / ${task.resolution} / ${task.modelCode || "AI 图像模型"}`;
    elements.status.textContent = labels.status[task.status] || task.status; elements.status.className = statusClass(task.status);
    elements.taskCreateTime.textContent = task.createTime || ""; elements.taskPrompt.textContent = task.prompt || "";
    elements.taskSpecTags.replaceChildren();
    const spec = task.designSpec || {};
    [labels.shape[spec.nailShape], labels.finish[spec.finish], labels.style[spec.designStyle], labels.layout[spec.layoutStyle], spec.colorPalette].filter(Boolean).forEach(value => { const tag = document.createElement("span"); tag.textContent = value; elements.taskSpecTags.append(tag); });
    elements.taskReference.replaceChildren();
    if (task.referenceAsset) {
      elements.taskReference.hidden = false; const image = document.createElement("img"); image.src = safeUrl(task.referenceAsset.url); image.alt = task.referenceAsset.name;
      const copy = document.createElement("div"); const eyebrow = document.createElement("span"); eyebrow.textContent = "REFERENCE ASSET"; const name = document.createElement("b"); name.textContent = task.referenceAsset.name; const note = document.createElement("small"); note.textContent = `${labels.reference[spec.referenceStrategy] || "参考图改款"} · ${task.referenceAsset.copyrightStatus}`;
      copy.append(eyebrow, name, note); elements.taskReference.append(image, copy);
    } else elements.taskReference.hidden = true;
    elements.taskResultCount.textContent = `${task.results?.length || 0} 张结果`;
    applyTaskSpec(task); elements.iterationPrompt.value = task.prompt || "";
    if (["QUEUED", "RUNNING"].includes(task.status)) showLoading();
    else if (task.results?.length) renderResults(task.results);
    else showError(task.errorMessage);
    updateRecord(task, access); renderRecords();
  }
  function renderResults(results) {
    const grid = document.createElement("div"); grid.className = `public-result-grid${results.length === 1 ? " is-single" : ""}`;
    results.forEach((item, index) => {
      const figure = document.createElement("figure"); const image = document.createElement("img"); image.src = safeUrl(item.url); image.alt = `AI 生成的美甲作品 ${index + 1}`; image.loading = "lazy";
      const caption = document.createElement("figcaption"); const meta = document.createElement("span"); meta.textContent = `方案 ${index + 1} · ${item.width} × ${item.height}`; const link = document.createElement("a"); link.href = safeUrl(item.url); link.target = "_blank"; link.rel = "noopener"; link.textContent = "查看原图 ↗";
      caption.append(meta, link); figure.append(image, caption); grid.append(figure);
    });
    elements.taskBody.replaceChildren(grid);
  }
  async function openRecord(record) {
    if (pollTimer) clearTimeout(pollTimer);
    currentAccess = { id: record.id, accessToken: record.accessToken };
    elements.newPanel.hidden = true; elements.detailPanel.hidden = false; elements.taskTitle.textContent = record.title; elements.status.textContent = "正在读取"; showLoading("正在读取设计档案");
    try { const task = await request(`/task/detail?id=${encodeURIComponent(record.id)}&token=${encodeURIComponent(record.accessToken)}`); renderTask(task, currentAccess); if (["QUEUED", "RUNNING"].includes(task.status)) schedulePoll(currentAccess); }
    catch (error) { elements.status.textContent = "读取失败"; showError(error.message); }
  }
  function schedulePoll(access) {
    if (pollTimer) clearTimeout(pollTimer);
    pollTimer = setTimeout(async () => {
      try {
        const task = await request(`/task/detail?id=${encodeURIComponent(access.id)}&token=${encodeURIComponent(access.accessToken)}`);
        renderTask(task, access); if (["QUEUED", "RUNNING"].includes(task.status)) schedulePoll(access);
      } catch (error) { showError(error.message); submitting = false; elements.generate.disabled = false; elements.iterationGenerate.disabled = false; }
    }, 2500);
  }
  async function ensureReferenceUploaded() {
    const file = elements.fileInput.files?.[0];
    if (!file) return referenceAssetId;
    if (referenceAssetId) return referenceAssetId;
    const data = new FormData(); data.append("file", file);
    const asset = await request("/reference", { method: "POST", body: data }); referenceAssetId = asset.id; return referenceAssetId;
  }
  async function createTask(promptValue, useCurrentReference = false) {
    if (submitting || promptValue.trim().length < 2) return;
    submitting = true; elements.generate.disabled = true; elements.iterationGenerate.disabled = true;
    try {
      const refId = useCurrentReference ? currentTask?.referenceAssetId : await ensureReferenceUploaded();
      const access = await request("/task/create", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(collectPayload(promptValue, refId)) });
      currentAccess = access;
      const provisional = { id: access.id, title: promptValue.trim().slice(0, 30), prompt: promptValue.trim(), status: "QUEUED", creativeMode, aspectRatio: elements.aspectRatio.value, resolution: elements.resolution.value, outputCount: Number(elements.outputCount.value), results: [], designSpec: collectPayload(promptValue, refId), createTime: new Date().toLocaleString() };
      renderTask(provisional, access); schedulePoll(access);
    } catch (error) { elements.detailPanel.hidden = false; elements.newPanel.hidden = true; elements.status.textContent = "创建失败"; showError(error.message); }
    finally { submitting = false; elements.generate.disabled = false; elements.iterationGenerate.disabled = false; }
  }

  document.querySelectorAll("[data-creative-mode]").forEach(button => button.addEventListener("click", () => { creativeMode = button.dataset.creativeMode; document.querySelectorAll("[data-creative-mode]").forEach(item => item.classList.toggle("is-active", item === button)); }));
  elements.trendOptions.addEventListener("click", event => {
    const button = event.target.closest("button"); if (!button) return;
    trendPreset = button.dataset.value; elements.colorPalette.value = button.dataset.palette; elements.finish.value = button.dataset.finish;
    elements.trendOptions.querySelectorAll("button").forEach(item => item.classList.toggle("is-active", item === button)); updateSummary();
  });
  [elements.nailShape, elements.finish, elements.designStyle, elements.layoutStyle, elements.colorPalette, elements.referenceStrategy, elements.aspectRatio, elements.resolution, elements.outputCount].forEach(field => field.addEventListener("change", updateSummary));
  document.querySelectorAll("[data-setting-target]").forEach(button => button.addEventListener("click", () => {
    const field = elements[button.dataset.settingTarget];
    if (!field) return;
    field.value = button.dataset.settingValue;
    field.dispatchEvent(new Event("change", { bubbles: true }));
  }));
  elements.parameterTrigger.addEventListener("click", () => setParametersOpen(elements.parameterPanel.hidden));
  elements.parameterClose.addEventListener("click", () => setParametersOpen(false, true));
  elements.parameterBackdrop.addEventListener("click", () => setParametersOpen(false, true));
  document.addEventListener("keydown", event => {
    if (elements.parameterPanel.hidden) return;
    if (event.key === "Escape") { setParametersOpen(false, true); return; }
    if (event.key !== "Tab") return;
    const focusable = [...elements.parameterPanel.querySelectorAll('button:not([disabled]):not([tabindex="-1"]), select:not([disabled]):not([tabindex="-1"]), input:not([disabled]):not([tabindex="-1"])')];
    const first = focusable[0]; const last = focusable[focusable.length - 1];
    if (!first || !last) return;
    if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
    else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
  });
  elements.prompt.addEventListener("input", () => { elements.promptCount.textContent = elements.prompt.value.length; });
  elements.upload.addEventListener("click", () => elements.fileInput.click());
  elements.fileInput.addEventListener("change", () => {
    const file = elements.fileInput.files?.[0]; if (!file) return;
    if (!["image/png", "image/jpeg"].includes(file.type) || file.size > 10 * 1024 * 1024) { elements.fileInput.value = ""; alert("请选择 10MB 以内的 PNG 或 JPG 图片"); return; }
    if (objectUrl) URL.revokeObjectURL(objectUrl); objectUrl = URL.createObjectURL(file); elements.previewImage.src = objectUrl; elements.fileName.textContent = file.name;
    elements.upload.hidden = true; elements.preview.hidden = false; elements.referenceStrategyField.hidden = false; referenceAssetId = undefined;
  });
  elements.remove.addEventListener("click", () => {
    elements.fileInput.value = ""; referenceAssetId = undefined; elements.preview.hidden = true; elements.upload.hidden = false; elements.referenceStrategyField.hidden = true;
    if (objectUrl) URL.revokeObjectURL(objectUrl); objectUrl = undefined; elements.previewImage.removeAttribute("src");
  });
  form.addEventListener("submit", event => { event.preventDefault(); createTask(elements.prompt.value); });
  elements.iterationForm.addEventListener("submit", event => { event.preventDefault(); createTask(elements.iterationPrompt.value, true); });
  elements.newDesign.addEventListener("click", showNewDesign); elements.iterationNew.addEventListener("click", showNewDesign); elements.taskBackToCreate?.addEventListener("click", showNewDesign);
  elements.refreshRecords.addEventListener("click", async () => {
    const activeId = currentTask?.id;
    for (const record of records) {
      try {
        const task = await request(`/task/detail?id=${encodeURIComponent(record.id)}&token=${encodeURIComponent(record.accessToken)}`);
        record.title = task.title || record.title; record.status = task.status; record.createTime = task.createTime || record.createTime;
        record.coverUrl = task.results?.[0]?.url || record.coverUrl; record.resultCount = task.results?.length || 0;
      } catch { record.status = "FAILED"; }
    }
    persistRecords(); renderRecords();
    const active = records.find(item => item.id === activeId);
    if (active) await openRecord(active);
  });
  window.addEventListener("beforeunload", () => { if (objectUrl) URL.revokeObjectURL(objectUrl); if (pollTimer) clearTimeout(pollTimer); }, { once: true });

  if (entryPrompt) {
    elements.prompt.value = entryPrompt;
    elements.promptCount.textContent = String(entryPrompt.length);
    elements.prompt.focus({ preventScroll: true });
  }
  async function restoreCreationBridge() {
    if (!creationTicket) return;
    try {
      const response = await fetch(`${bridgeApi}/consume?ticket=${encodeURIComponent(creationTicket)}`, { headers: { Accept: "application/json" } });
      const payload = await response.json().catch(() => null);
      if (!response.ok || payload?.code !== 200 || !payload.data?.prompt) throw new Error(payload?.msg || "设计意图未能恢复");
      elements.prompt.value = payload.data.prompt;
      elements.promptCount.textContent = String(payload.data.prompt.length);
      if (mastheadSession) {
        mastheadSession.textContent = `${payload.data.displayName || "当前"} · ${payload.data.roleName || "用户"}创作卷`;
        mastheadSession.dataset.role = payload.data.role || "USER";
        mastheadSession.hidden = false;
      }
      history.replaceState({}, document.title, `${location.pathname}${location.hash}`);
      elements.prompt.focus({ preventScroll: true });
    } catch (error) {
      if (mastheadSession) {
        mastheadSession.textContent = error.message || "设计意图未能恢复，请返回首页重新开始";
        mastheadSession.hidden = false;
      }
    }
  }
  void restoreCreationBridge();
  if (mastheadSession && window.NailMemberAuth) {
    window.NailMemberAuth.session().then(session => {
      if (!session?.loggedIn) return;
      mastheadSession.textContent = `${session.displayName} · ${session.roleName || "用户"}创作卷`;
      mastheadSession.dataset.role = session.role || "USER";
      mastheadSession.hidden = false;
    }).catch(() => {});
  }
  enhanceInspectorSelects(); updateSummary(); renderRecords();
});
