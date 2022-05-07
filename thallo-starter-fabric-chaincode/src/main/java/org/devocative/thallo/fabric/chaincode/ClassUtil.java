package org.devocative.thallo.fabric.chaincode;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ClassUtil {

	public static Set<String> findBasePackages(ApplicationContext context) {
		final Set<String> result = new HashSet<>();

		final Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(ComponentScan.class);
		final String basePackage = beansWithAnnotation.values().stream()
			.map(o -> o.getClass().getPackageName())
			.findFirst()
			.orElseThrow();

		result.add(basePackage);

		return result;
	}

	public static void scanPackagesForAnnotatedClasses(Class<? extends Annotation> annotationClass, Set<String> basePackages, Consumer<BeanDefinition> consumer) {
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

		final Set<String> seenBeans = new HashSet<>();

		for (String basePackage : basePackages) {
			for (BeanDefinition beanDefinition : provider.findCandidateComponents(basePackage)) {
				final String beanClassName = beanDefinition.getBeanClassName();
				if (!seenBeans.contains(beanClassName)) {
					consumer.accept(beanDefinition);
					seenBeans.add(beanClassName);
				}
			}
		}
	}
}
