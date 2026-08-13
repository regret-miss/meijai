document.addEventListener("DOMContentLoaded", () => {
  const fileName = decodeURIComponent(location.pathname.split("/").pop() || "首页.html");
  const routes = [
    ["首页.html", "首页"],
    ["美甲款式.html", "美甲款式"],
    ["美甲师.html", "美甲师"],
    ["AI.html", "AI 创作"],
    ["会员福利.html", "会员福利"],
    ["首页.html#contact", "联系我们"]
  ];

  let header = document.getElementById("siteHeader");
  if (!header || !header.classList.contains("site-header")) {
    const legacyHeader = document.querySelector("body > header");
    const wrapper = document.createElement("div");
    wrapper.innerHTML = `<header class="site-header" id="siteHeader"><div class="header-inner"><a class="brand" href="首页.html" aria-label="指尖湘韵首页"><span class="brand-seal" aria-hidden="true">湘</span><span class="brand-copy"><span class="brand-name">指尖湘韵</span><span class="brand-en">NAIL ART · XIANG</span></span></a><span class="header-note">以甲为纸 · 以色入画</span><nav class="site-nav" id="siteNav" aria-label="网站导航"></nav><button class="nav-menu" id="navMenu" type="button" aria-label="打开导航" aria-expanded="false"><span></span></button></div></header>`;
    header = wrapper.firstElementChild;
    if (legacyHeader) legacyHeader.replaceWith(header);
    else document.body.prepend(header);
  }

  const nav = header.querySelector("#siteNav");
  if (nav) {
    const links = routes.map(([href, label]) => {
      const link = document.createElement("a");
      link.href = href;
      link.textContent = label;
      const routeFile = href.split("#")[0];
      const isActive = fileName === routeFile && !href.includes("#");
      if (isActive) {
        link.className = "is-active";
        link.setAttribute("aria-current", "page");
      }
      return link;
    });
    const marker = document.createElement("span");
    marker.className = "nav-indicator";
    marker.id = "navIndicator";
    marker.setAttribute("aria-hidden", "true");
    nav.replaceChildren(...links, marker);
  }

  const note = header.querySelector(".header-note");
  if (fileName === "AI.html" && note) note.textContent = "AI 美甲设计工作台";
  const menu = header.querySelector("#navMenu");
  const indicator = header.querySelector("#navIndicator");
  const active = nav?.querySelector("a.is-active");

  const scrollSentinel = document.createElement("span");
  scrollSentinel.className = "nav-scroll-sentinel";
  scrollSentinel.setAttribute("aria-hidden", "true");
  document.body.prepend(scrollSentinel);
  const syncHeader = (atTop = true) => header.classList.toggle("is-scrolled", !atTop);
  const syncIndicator = () => {
    if (!nav || !indicator || !active || window.innerWidth <= 980) return;
    const navRect = nav.getBoundingClientRect();
    const linkRect = active.getBoundingClientRect();
    indicator.style.width = `${Math.max(18, linkRect.width - 30)}px`;
    indicator.style.transform = `translateX(${linkRect.left - navRect.left + 15}px)`;
  };

  menu?.addEventListener("click", () => {
    const isOpen = nav?.classList.toggle("is-open") || false;
    menu.classList.toggle("is-open", isOpen);
    menu.setAttribute("aria-expanded", String(isOpen));
    menu.setAttribute("aria-label", isOpen ? "关闭导航" : "打开导航");
  });
  nav?.querySelectorAll("a").forEach((link) => link.addEventListener("click", () => {
    nav.classList.remove("is-open");
    menu?.classList.remove("is-open");
    menu?.setAttribute("aria-expanded", "false");
  }));

  window.addEventListener("resize", syncIndicator, { passive: true });
  if ("IntersectionObserver" in window) {
    new IntersectionObserver(([entry]) => syncHeader(entry.isIntersecting), { threshold: 0 }).observe(scrollSentinel);
  } else syncHeader(true);
  requestAnimationFrame(syncIndicator);
});
