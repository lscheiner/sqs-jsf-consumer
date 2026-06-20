package br.com.scheiner.aws.console.web.navigation;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.faces.context.FacesContext;

@Component
public class NavigationManager {

	public Optional<String> getRequestedResource(ApplicationRoute route) {
		var context = FacesContext.getCurrentInstance();
		if (context == null || route.getResourceParameter() == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(context.getExternalContext()
				.getRequestParameterMap()
				.get(route.getResourceParameter()))
				.filter(value -> !value.isBlank());
	}

	public void redirect(ApplicationRoute route) throws IOException {
		this.redirect(route, null);
	}

	public void redirect(ApplicationRoute route, String resourceIdentifier) throws IOException {
		if (route == null) {
			return;
		}
		var context = FacesContext.getCurrentInstance();
		var contextPath = context.getExternalContext().getRequestContextPath();
		context.getExternalContext().redirect(contextPath + route.getPath(resourceIdentifier));
	}
}
