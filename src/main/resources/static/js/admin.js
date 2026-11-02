// Переключатель «ссылка / файл» и предпросмотр картинки в форме поста
const preview = document.getElementById('imagePreview');
function syncImageSource() {
    const mode = document.querySelector('input[name="imageSource"]:checked')?.value || 'url';
    document.querySelectorAll('[data-image-pane]').forEach(pane => {
        const on = pane.dataset.imagePane === mode;
        pane.hidden = !on;
        pane.querySelectorAll('input').forEach(i => i.disabled = !on);
    });
    updatePreview();
}
function updatePreview() {
    if (!preview) return;
    const mode = document.querySelector('input[name="imageSource"]:checked')?.value || 'url';
    let src = '';
    if (mode === 'file') {
        const file = document.querySelector('input[name="imageFile"]')?.files[0];
        if (file) src = URL.createObjectURL(file);
    } else {
        const url = document.querySelector('input[name="imageUrl"]')?.value.trim();
        if (/^https?:\/\//.test(url || '')) src = url;
    }
    preview.hidden = !src;
    if (src) preview.src = src; else preview.removeAttribute('src');
}
if (document.querySelector('[data-image-pane]')) {
    document.querySelectorAll('input[name="imageSource"]').forEach(r => r.addEventListener('change', syncImageSource));
    document.querySelector('input[name="imageFile"]')?.addEventListener('change', updatePreview);
    document.querySelector('input[name="imageUrl"]')?.addEventListener('input', updatePreview);
    preview?.addEventListener('error', () => { preview.hidden = true; });
    syncImageSource();
}

// Мини-окно пользователя
const userModal = document.getElementById('modal-user');
async function openUser(id, keepFlash) {
    if (!userModal) return;
    if (!keepFlash) document.getElementById('userFlash').innerHTML = '';
    const box = document.getElementById('userCard');
    box.innerHTML = '<p class="muted">Загрузка…</p>';
    userModal.classList.add('modal--open');
    const q = encodeURIComponent(document.body.dataset.q || '');
    try {
        const res = await fetch(`/admin/users/${encodeURIComponent(id)}/card?q=${q}`, { headers: { Accept: 'text/html' } });
        box.innerHTML = res.ok ? await res.text() : '<div class="alert alert--error">Не удалось загрузить пользователя</div>';
    } catch {
        box.innerHTML = '<div class="alert alert--error">Нет связи с сервером</div>';
    }
}
function closeUser() { userModal?.classList.remove('modal--open'); }

document.addEventListener('click', e => {
    const row = e.target.closest('[data-user-id]');
    if (row) { openUser(row.dataset.userId, false); return; }
    if (userModal && (e.target === userModal || e.target.closest('#modal-user [data-close]'))) closeUser();
});
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeUser(); });
if (document.body.dataset.openUser) openUser(document.body.dataset.openUser, true);
