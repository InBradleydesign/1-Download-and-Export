const topbar = document.getElementById('topbar');
const menu = document.getElementById('menu');
const menuToggle = document.getElementById('menu-toggle');
const stickyDownload = document.querySelector('.sticky-download');
const heroActions = document.querySelector('.hero-actions');
const downloadSection = document.getElementById('baixar');

menuToggle.addEventListener('click', () => {
    const open = menu.classList.toggle('open');
    menuToggle.setAttribute('aria-expanded', open);
});

menu.querySelectorAll('a').forEach(link => link.addEventListener('click', () => {
    menu.classList.remove('open');
    menuToggle.setAttribute('aria-expanded', 'false');
}));

window.addEventListener('scroll', () => {
    topbar.classList.toggle('scrolled', window.scrollY > 8);
}, { passive: true });

// Barra fixa de download no celular: aparece depois do botão do topo e some na seção de download
let heroVisible = true;
let downloadVisible = false;
const updateSticky = () => stickyDownload.classList.toggle('show', !heroVisible && !downloadVisible);
new IntersectionObserver(([entry]) => { heroVisible = entry.isIntersecting; updateSticky(); }).observe(heroActions);
new IntersectionObserver(([entry]) => { downloadVisible = entry.isIntersecting; updateSticky(); }).observe(downloadSection);

// Perfil (idoso / cuidador)
document.querySelectorAll('[data-profile]').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('[data-profile]').forEach(b => {
            const active = b === btn;
            b.classList.toggle('active', active);
            b.setAttribute('aria-selected', active);
        });
        document.querySelectorAll('[data-showcase]').forEach(s => {
            s.hidden = s.dataset.showcase !== btn.dataset.profile;
        });
    });
});

// Troca da tela do celular ao escolher um recurso
document.querySelectorAll('.showcase').forEach(showcase => {
    const img = showcase.querySelector('.showcase-phone img');
    showcase.querySelectorAll('.feature').forEach(feature => {
        feature.addEventListener('click', () => {
            showcase.querySelectorAll('.feature').forEach(f => f.classList.toggle('active', f === feature));
            if (img.getAttribute('src') === feature.dataset.img) return;
            img.classList.add('fading');
            setTimeout(() => {
                img.onload = () => img.classList.remove('fading');
                img.src = feature.dataset.img;
                img.alt = feature.dataset.alt;
            }, 180);
        });
    });
    showcase.querySelectorAll('.feature').forEach(f => { new Image().src = f.dataset.img; });
});

// Animação de entrada
const reveal = new IntersectionObserver(entries => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('visible');
            reveal.unobserve(entry.target);
        }
    });
}, { threshold: 0.12 });

document.querySelectorAll('.section-head, .stat, .callout > div, .pillar, .showcase, .result, .evidence, .benefit, .privacy > *, .download, .faq details')
    .forEach(el => { el.classList.add('reveal'); reveal.observe(el); });
