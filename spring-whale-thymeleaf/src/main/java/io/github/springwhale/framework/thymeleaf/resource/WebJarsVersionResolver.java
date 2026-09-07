package io.github.springwhale.framework.thymeleaf.resource;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;
import org.webjars.WebJarVersionLocator;

import java.util.List;

/**
 * Resolves version-agnostic WebJar paths (e.g. {@code /webjars/bootstrap/css/bootstrap.min.css})
 * to their versioned classpath location (e.g. {@code bootstrap/5.3.8/css/bootstrap.min.css})
 * using {@link WebJarVersionLocator}, so templates never hard-code a WebJar version.
 *
 * <p>Requests that already carry a version or reference an unknown WebJar fall through
 * to the default resolution chain.</p>
 */
public class WebJarsVersionResolver extends AbstractResourceResolver {

    private final WebJarVersionLocator locator;

    public WebJarsVersionResolver(WebJarVersionLocator locator) {
        this.locator = locator;
    }

    @Override
    protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
                                               List<? extends Resource> locations, ResourceResolverChain chain) {
        String versionedPath = toVersionedPath(requestPath);
        if (versionedPath != null) {
            Resource resource = chain.resolveResource(request, versionedPath, locations);
            if (resource != null) {
                return resource;
            }
        }
        return chain.resolveResource(request, requestPath, locations);
    }

    @Override
    protected String resolveUrlPathInternal(String resourceUrlPath,
                                            List<? extends Resource> locations, ResourceResolverChain chain) {
        String versionedPath = toVersionedPath(resourceUrlPath);
        if (versionedPath != null) {
            String resolved = chain.resolveUrlPath(versionedPath, locations);
            if (resolved != null) {
                return resolved;
            }
        }
        return chain.resolveUrlPath(resourceUrlPath, locations);
    }

    /**
     * @return versioned path like {@code bootstrap/5.3.8/css/bootstrap.min.css},
     * or {@code null} if the path cannot be mapped to a known WebJar
     */
    private String toVersionedPath(String path) {
        if (path == null) {
            return null;
        }
        int slash = path.indexOf('/');
        if (slash <= 0) {
            return null;
        }
        String webJar = path.substring(0, slash);
        String filePath = path.substring(slash + 1);
        return locator.path(webJar, filePath);
    }
}
