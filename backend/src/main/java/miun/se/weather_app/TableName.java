package miun.se.weather_app;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "table_name", schema = "app")
public class TableName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Column(name = "MSG", length = 50)
    private String msg;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}