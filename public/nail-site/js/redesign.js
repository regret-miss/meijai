document.addEventListener("DOMContentLoaded", () => {
  const header = document.getElementById("siteHeader");
  const nav = document.getElementById("siteNav");
  const menu = document.getElementById("navMenu");
  const indicator = document.getElementById("navIndicator");
  const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  const updateIndicator = (link) => {
    if (!indicator || !nav || !link || window.innerWidth <= 980) return;
    const navRect = nav.getBoundingClientRect();
    const linkRect = link.getBoundingClientRect();
    indicator.style.width = `${linkRect.width - 30}px`;
    indicator.style.transform = `translateX(${linkRect.left - navRect.left + 15}px)`;
  };

  const setActiveLink = (id) => {
    if (!nav) return;
    const links = [...nav.querySelectorAll("a")];
    const active = links.find((link) => link.dataset.section === id) || links.find((link) => link.classList.contains("is-active"));
    links.forEach((link) => {
      const isActive = link === active;
      link.classList.toggle("is-active", isActive);
      if (isActive) link.setAttribute("aria-current", "page");
      else link.removeAttribute("aria-current");
    });
    updateIndicator(active);
  };

  if (menu && nav) {
    menu.addEventListener("click", () => {
      const isOpen = nav.classList.toggle("is-open");
      menu.classList.toggle("is-open", isOpen);
      menu.setAttribute("aria-expanded", String(isOpen));
      menu.setAttribute("aria-label", isOpen ? "关闭导航" : "打开导航");
    });
    nav.querySelectorAll("a").forEach((link) => {
      link.addEventListener("click", () => {
        nav.classList.remove("is-open");
        menu.classList.remove("is-open");
        menu.setAttribute("aria-expanded", "false");
      });
    });
  }

  const activeLink = nav?.querySelector("a.is-active");
  if (activeLink) requestAnimationFrame(() => updateIndicator(activeLink));
  if (nav && "ResizeObserver" in window) {
    const navResizeObserver = new ResizeObserver(() => updateIndicator(nav.querySelector("a.is-active")));
    navResizeObserver.observe(nav);
  }

  const homeSections = [...document.querySelectorAll("[data-nav-section]")];
  if (homeSections.length && "IntersectionObserver" in window) {
    const sectionObserver = new IntersectionObserver((entries) => {
      const visible = entries.filter((entry) => entry.isIntersecting).sort((a, b) => b.intersectionRatio - a.intersectionRatio);
      if (visible[0]) setActiveLink(visible[0].target.id);
    }, { rootMargin: "-22% 0px -62% 0px", threshold: [0, .1, .25, .5] });
    homeSections.forEach((section) => sectionObserver.observe(section));
  }

  const hero = document.getElementById("home");
  if (header && hero && "IntersectionObserver" in window) {
    const headerObserver = new IntersectionObserver(([entry]) => {
      header.classList.toggle("is-scrolled", !entry.isIntersecting);
    }, { rootMargin: "-90px 0px 0px", threshold: 0 });
    headerObserver.observe(hero);
  }

  const revealItems = [...document.querySelectorAll(".reveal")];
  if (!reducedMotion && "IntersectionObserver" in window) {
    const revealObserver = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("is-visible");
          revealObserver.unobserve(entry.target);
        }
      });
    }, { threshold: .08, rootMargin: "0px 0px -6% 0px" });
    revealItems.forEach((item) => revealObserver.observe(item));
  } else {
    revealItems.forEach((item) => item.classList.add("is-visible"));
  }

  const contactForm = document.getElementById("contactForm");
  if (contactForm) {
    contactForm.addEventListener("submit", (event) => {
      event.preventDefault();
      const button = contactForm.querySelector("button[type='submit']");
      button.textContent = "已收到，我们会尽快联系你";
      button.disabled = true;
    });
  }

  const aiForm = document.getElementById("aiForm");
  const aiInput = document.getElementById("aiInput");
  const sendButton = document.getElementById("sendButton");
  const charCount = document.getElementById("charCount");
  const responseArea = document.getElementById("responseArea");
  const responseBody = document.getElementById("responseBody");
  const responseStatus = document.getElementById("responseStatus");
  const uploadButton = document.getElementById("uploadButton");
  const imageUpload = document.getElementById("imageUpload");
  const uploadPreview = document.getElementById("uploadPreview");
  const previewImage = document.getElementById("previewImage");
  let previewUrl = "";

  const resizeTextarea = () => {
    if (!aiInput) return;
    aiInput.style.height = "auto";
    aiInput.style.height = `${Math.min(aiInput.scrollHeight, 210)}px`;
  };

  const syncComposer = () => {
    if (!aiInput || !sendButton || !charCount) return;
    const length = aiInput.value.trim().length;
    charCount.textContent = `${aiInput.value.length} / 500`;
    sendButton.disabled = length === 0 && !imageUpload?.files?.length;
    resizeTextarea();
  };

  const buildResponse = (prompt) => {
    const text = prompt || "参考图片中的色彩与质感";
    let palette = "黛青、雾灰与一点朱砂红";
    let texture = "半透水墨晕染，局部用极细金线收边";
    let shape = "短方圆甲型";
    if (/婚|新娘/.test(text)) {
      palette = "月白、柔金与低饱和胭脂色";
      texture = "珍珠微光底，点缀克制的金箔与单颗小钻";
      shape = "中短杏仁甲型";
    } else if (/猫眼|约会|七夕/.test(text)) {
      palette = "烟粉、深莓红与冷调银光";
      texture = "细闪猫眼铺底，两指加入留白花枝";
      shape = "短杏仁甲型";
    } else if (/通勤|短甲|水墨/.test(text)) {
      palette = "青黛、米灰与透明裸色";
      texture = "低对比水墨晕染，用一笔朱红作为视觉落款";
      shape = "短方圆甲型";
    }
    return `
      <h3>为你定制：山色有无</h3>
      <dl>
        <div><dt>甲型建议</dt><dd>${shape}，利落耐看，也更适合日常活动。</dd></div>
        <div><dt>配色方案</dt><dd>${palette}，显白但不过分抢眼。</dd></div>
        <div><dt>纹样设计</dt><dd>${texture}，十指保持疏密变化。</dd></div>
        <div><dt>工艺提示</dt><dd>建议薄涂建构并加强前缘，预计用时约 120 分钟。</dd></div>
      </dl>
      <div class="response-actions"><a class="secondary-btn" href="美甲款式.html">查看相近作品</a></div>
    `;
  };

  if (aiInput) aiInput.addEventListener("input", syncComposer);
  document.querySelectorAll(".prompt-chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      aiInput.value = chip.textContent.trim();
      syncComposer();
      aiInput.focus();
    });
  });

  if (uploadButton && imageUpload) uploadButton.addEventListener("click", () => imageUpload.click());
  if (imageUpload) {
    imageUpload.addEventListener("change", () => {
      const file = imageUpload.files?.[0];
      if (!file) return;
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      previewUrl = URL.createObjectURL(file);
      previewImage.src = previewUrl;
      uploadPreview.classList.add("is-visible");
      uploadButton.textContent = "已添加参考图";
      syncComposer();
    });
  }

  if (aiForm) {
    aiForm.addEventListener("submit", (event) => {
      event.preventDefault();
      if (sendButton.disabled) return;
      responseArea.classList.add("is-visible");
      responseStatus.textContent = "正在研色";
      responseBody.innerHTML = '<div class="skeleton" aria-label="方案生成中"><span></span><span></span><span></span></div>';
      sendButton.disabled = true;
      setTimeout(() => {
        responseStatus.textContent = "方案已完成";
        responseBody.innerHTML = buildResponse(aiInput.value.trim());
        responseArea.scrollIntoView({ behavior: reducedMotion ? "auto" : "smooth", block: "nearest" });
        sendButton.disabled = false;
      }, 720);
    });
  }

  window.addEventListener("beforeunload", () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
  }, { once: true });
});
