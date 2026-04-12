package devs.group5.rms.entities;

import lombok.ToString;

@ToString
public enum Role {
    ADMIN,
    OWNER;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
