package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Category;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "categories",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_uid", "name"}))
public class CategoryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon")
    private String icon;

    @Column(name = "budget", precision = 19, scale = 2)
    private BigDecimal budget;

    @Column(name = "color")
    private String color;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "nature")
    private String nature;

    public CategoryEntity() {}

    public static CategoryEntity fromDomain(Category c) {
        CategoryEntity e = new CategoryEntity();
        e.id = c.getId();
        e.userUid = c.getUserUid();
        e.name = c.getName();
        e.icon = c.getIcon();
        e.budget = c.getBudget();
        e.color = c.getColor();
        e.type = c.getType();
        e.nature = c.getNature();
        return e;
    }

    public Category toDomain() {
        Category c = new Category(userUid, name, icon, budget, color, type, nature);
        c.setId(id);
        return c;
    }

    public UUID getId()            { return id; }
    public String getUserUid()     { return userUid; }
    public String getName()        { return name; }
    public String getIcon()        { return icon; }
    public BigDecimal getBudget()  { return budget; }
    public String getColor()       { return color; }
    public String getType()        { return type; }
    public String getNature()      { return nature; }

    public void setId(UUID id)            { this.id = id; }
    public void setUserUid(String uid)    { this.userUid = uid; }
    public void setName(String name)      { this.name = name; }
    public void setIcon(String icon)      { this.icon = icon; }
    public void setBudget(BigDecimal b)   { this.budget = b; }
    public void setColor(String color)    { this.color = color; }
    public void setType(String type)      { this.type = type; }
    public void setNature(String nature)  { this.nature = nature; }
}
