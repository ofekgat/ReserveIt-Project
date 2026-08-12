package getticket.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/** Shared helper for showing FacesMessages, used by the backing beans instead of each repeating it. */
final class FacesMessages {

    private FacesMessages() {
    }

    static void addError(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
    }
}
