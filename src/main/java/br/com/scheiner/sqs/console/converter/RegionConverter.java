package br.com.scheiner.sqs.console.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import software.amazon.awssdk.regions.Region;

@FacesConverter(value = "regionConverter", managed = true)
public class RegionConverter implements Converter<Region> {

    @Override
    public Region getAsObject(
            FacesContext context,
            UIComponent component,
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Region.of(value);
    }

    @Override
    public String getAsString(
            FacesContext context,
            UIComponent component,
            Region value) {

        if (value == null) {
            return "";
        }

        return value.id();
    }
}
