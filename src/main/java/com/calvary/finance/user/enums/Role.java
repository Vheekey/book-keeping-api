package com.calvary.finance.user.enums;

import lombok.Getter;

@Getter
public enum Role {
    USER(1),
    SADMIN(2),
    ADMIN(3),
    FINANCE(4);

    private final int id;

    Role(int id){
        this.id = id;
    }

    public static Role getRoleById(int id){
        for(Role role: Role.values()){
            if(role.getId() == id){
                return role;
            }
        }
        return null;
    }

}
