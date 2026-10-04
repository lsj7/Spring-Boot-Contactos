function csrfHeaders(headers) {
    const token = document.querySelector('meta[name="csrf-token"]')?.content;
    const headerName = document.querySelector('meta[name="csrf-header"]')?.content || "X-CSRF-TOKEN";
    if (token) {
        headers[headerName] = token;
    }
    return headers;
}

function showFeedback(form, message) {
    const feedback = form.querySelector("[data-feedback]");
    if (!feedback) return;
    feedback.textContent = message;
    feedback.hidden = false;
}

async function responseMessage(response) {
    const body = await response.json().catch(() => ({}));
    if (Array.isArray(body.detail)) {
        return body.detail.map((item) => item.msg).join(" ");
    }
    return body.detail || "No se pudo completar la solicitud. Inténtalo de nuevo.";
}

const contactForm = document.querySelector("[data-contact-form]");
if (contactForm) {
    contactForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const button = contactForm.querySelector("button[type='submit']");
        button.disabled = true;
        const contactId = contactForm.dataset.contactId;
        const fields = new FormData(contactForm);
        try {
            const response = await fetch(contactId ? `/agenda/${contactId}` : "/agenda", {
                method: contactId ? "PUT" : "POST",
                credentials: "same-origin",
                headers: csrfHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify({
                    nombre: fields.get("nombre"),
                    telefono: fields.get("telefono"),
                    mail: fields.get("mail"),
                }),
            });
            if (!response.ok) throw new Error(await responseMessage(response));
            window.location.assign("/views/agenda");
        } catch (error) {
            showFeedback(contactForm, error.message);
        } finally {
            button.disabled = false;
        }
    });
}

document.querySelectorAll("[data-delete-contact]").forEach((button) => {
    button.addEventListener("click", async () => {
        if (!window.confirm("¿Eliminar este contacto? Esta acción no se puede deshacer.")) return;

        button.disabled = true;
        try {
            const response = await fetch(`/agenda/${button.dataset.deleteContact}`, {
                method: "DELETE",
                credentials: "same-origin",
                headers: csrfHeaders({}),
            });
            if (!response.ok) throw new Error(await responseMessage(response));
            window.location.reload();
        } catch (error) {
            window.alert(error.message);
            button.disabled = false;
        }
    });
});

const searchInput = document.querySelector("[data-contact-search]");
if (searchInput) {
    const rows = [...document.querySelectorAll("[data-contact-row]")];
    const noResults = document.querySelector("[data-no-results]");
    searchInput.addEventListener("input", () => {
        const query = searchInput.value.trim().toLocaleLowerCase();
        let visibleRows = 0;
        rows.forEach((row) => {
            const matches = row.textContent.toLocaleLowerCase().includes(query);
            row.hidden = !matches;
            if (matches) visibleRows += 1;
        });
        if (noResults) noResults.hidden = visibleRows > 0;
    });
}
