const MODALS = ['login', 'register'];

function openModal(name) {
    if (!MODALS.includes(name)) return;
    const modal = document.getElementById('modal-' + name);
    if (!modal) return;
    modal.classList.add('modal--open');
    modal.querySelector('input')?.focus();
}

function closeAll() {
    document.querySelectorAll('.modal--open').forEach(m => m.classList.remove('modal--open'));
}

async function toggleLike(btn) {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;
    btn.disabled = true;
    try {
        const res = await fetch(`/api/posts/${btn.dataset.postId}/like`, {
            method: 'POST',
            headers: { [header]: token, 'Accept': 'application/json' }
        });
        if (res.status === 401) { openModal('login'); return; }
        if (!res.ok) return;
        const data = await res.json();
        btn.classList.toggle('like--active', data.liked);
        btn.querySelector('.like__count').textContent = data.count;
    } finally {
        btn.disabled = false;
    }
}

document.addEventListener('click', e => {
    const opener = e.target.closest('[data-open]');
    if (opener) { e.preventDefault(); openModal(opener.dataset.open); return; }

    const switcher = e.target.closest('[data-switch]');
    if (switcher) { e.preventDefault(); closeAll(); openModal(switcher.dataset.switch); return; }

    if (e.target.closest('[data-close]') || e.target.classList.contains('modal')) { closeAll(); return; }

    const like = e.target.closest('.like[data-post-id]');
    if (like) toggleLike(like);
});

document.addEventListener('keydown', e => { if (e.key === 'Escape') closeAll(); });

const auto = document.getElementById('autoOpen')?.dataset.modal;
if (auto) openModal(auto);
