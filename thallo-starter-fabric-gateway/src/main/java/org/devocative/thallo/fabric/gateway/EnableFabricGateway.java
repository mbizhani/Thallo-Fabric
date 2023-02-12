package org.devocative.thallo.fabric.gateway;

import org.devocative.thallo.fabric.gateway.scanner.FabricClientsRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({FabricClientsRegistrar.class})
public @interface EnableFabricGateway {
	String[] value() default {};

	String[] basePackages() default {};

	Class<?>[] basePackageClasses() default {};
}
