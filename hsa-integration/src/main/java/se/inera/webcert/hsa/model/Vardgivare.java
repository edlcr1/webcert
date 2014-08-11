package se.inera.webcert.hsa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author andreaskaltenbach
 */
public class Vardgivare implements SelectableVardenhet, Serializable {

    private static final long serialVersionUID = 4462766290949153158L;

    private String id;
    private String namn;

    private List<Vardenhet> vardenheter;

    public Vardgivare() {
    }

    public Vardgivare(String id, String namn) {
        this.id = id;
        this.namn = namn;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Vardenhet> getVardenheter() {

        if (vardenheter == null) {
            vardenheter = new ArrayList<>();
        }

        return vardenheter;
    }

    public void setVardenheter(List<Vardenhet> vardenheter) {
        this.vardenheter = vardenheter;
    }

    @JsonIgnore
    public List<String> getHsaIds() {
        List<String> ids = new ArrayList<>();
        for (Vardenhet vardenhet : getVardenheter()) {
            ids.addAll(vardenhet.getHsaIds());
        }
        return ids;
    }

    public SelectableVardenhet findVardenhet(String id) {

        if (id == null) {
            return null;
        }

        SelectableVardenhet sve = null;

        for (Vardenhet vardenhet : getVardenheter()) {
            sve = vardenhet.findSelectableVardenhet(id);
            if (sve != null) {
                return sve;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Vardgivare that = (Vardgivare) o;

            if (id == null) {
                return that.id == null;
            } else {
                return id.equals(that.id);
            }
        }
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
