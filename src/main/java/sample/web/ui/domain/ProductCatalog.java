package sample.web.ui.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class ProductCatalog {

    @Id
    @GeneratedValue
    private Long id;


    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    private List<Product> products = new ArrayList<>();


    public void add(Product p) {
        products.add(p);
    }

    public Product find(Long id) {
        for(Product p : products) {
            if(p.getId() == id)
                return p;
        }
        return null;
    }
}