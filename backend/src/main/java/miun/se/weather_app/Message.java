package miun.se.weather_app;

import jakarta.persistence.*;

@Entity
@Table(name = "messages")
@NamedQuery(name = "Message.findAll", query = "SELECT m FROM Message m")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "msg")
    private String msg;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}