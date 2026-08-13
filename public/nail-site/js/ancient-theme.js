document.addEventListener('DOMContentLoaded', () => {
  const body = document.body;
  body.classList.add('ancient-motion-ready');

  const revealTargets = [
    ...document.querySelectorAll('main > section:not(.ai-hero), .nail-item, .artist-grid > div')
  ];

  revealTargets.forEach((element, index) => {
    element.classList.add('ancient-reveal');
    element.style.setProperty('--reveal-delay', `${Math.min(index % 6, 5) * 55}ms`);
  });

  if ('IntersectionObserver' in window && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.08, rootMargin: '0px 0px -5% 0px' });
    revealTargets.forEach((element) => observer.observe(element));
  } else {
    revealTargets.forEach((element) => element.classList.add('is-visible'));
  }

  const menuToggle = document.querySelector('.site-menu-toggle');
  const siteLinks = document.querySelector('.site-links');
  if (menuToggle && siteLinks) {
    const iconPrefix = body.classList.contains('catalog-page') ? 'fa-solid' : 'fa';
    menuToggle.addEventListener('click', () => {
      const isOpen = siteLinks.classList.toggle('is-open');
      menuToggle.setAttribute('aria-expanded', String(isOpen));
      menuToggle.innerHTML = `<i class="${iconPrefix} ${isOpen ? 'fa-times' : 'fa-bars'}" aria-hidden="true"></i>`;
    });
    siteLinks.querySelectorAll('a').forEach((link) => {
      link.addEventListener('click', () => {
        siteLinks.classList.remove('is-open');
        menuToggle.setAttribute('aria-expanded', 'false');
        menuToggle.innerHTML = `<i class="${iconPrefix} fa-bars" aria-hidden="true"></i>`;
      });
    });
  }
});
