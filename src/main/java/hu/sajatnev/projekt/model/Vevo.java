package hu.sajatnev.projekt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "VEVOK")
public class Vevo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle esetén érdemes lehet SEQUENCE-re váltani, ha van kész script. IDENTITY kell, ha a
    @Column(name = "VEVO_ID")
    private Long vevoId;

    @Column(name = "KERESZTNEV", length = 50)
    private String keresztnev;

    @Column(name = "VEZETEKNEV", length = 50)
    private String vezeteknev;

    @Column(name = "EMAIL", length = 100, unique = true)
    private String email;

    @Column(name = "VAROS", length = 50)
    private String varos;

    @Column(name = "PW_HASH", length = 128)
    private String pwHash;

    // Alapértelmezett üres konstruktor (a JPA-nak kötelező)
    public Vevo() {}

    // Getterek és Setterek
    public Long getVevoId() { return vevoId; }
    public void setVevoId(Long vevoId) { this.vevoId = vevoId; }

    public String getKeresztnev() { return keresztnev; }
    public void setKeresztnev(String keresztnev) { this.keresztnev = keresztnev; }

    public String getVezeteknev() { return vezeteknev; }
    public void setVezeteknev(String vezeteknev) { this.vezeteknev = vezeteknev; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVaros() { return varos; }
    public void setVaros(String varos) { this.varos = varos; }

    public String getPwHash() { return pwHash; }
    public void setPwHash(String pwHash) { this.pwHash = pwHash; }
}
