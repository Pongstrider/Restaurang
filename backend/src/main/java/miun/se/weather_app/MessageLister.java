package miun.se.weather_app;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;

@Named("messagelister")
@RequestScoped
public class MessageLister {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    private String newMsg;

    // Required so the XHTML form can pass the input value back
    public String getNewMsg() {
        return newMsg;
    }

    public void setNewMsg(String newMsg) {
        this.newMsg = newMsg;
    }

    @Transactional
    public void save() {
        if (newMsg != null && !newMsg.trim().isEmpty()) {
            Message message = new Message();
            message.setMsg(newMsg);
            em.persist(message);
            newMsg = ""; // Clear input after saving
        }
    }

    public List<Message> getMessages(){
        TypedQuery<Message> namedQuery = em.createNamedQuery("Message.findAll", Message.class);
        return namedQuery.getResultList();
    }
}