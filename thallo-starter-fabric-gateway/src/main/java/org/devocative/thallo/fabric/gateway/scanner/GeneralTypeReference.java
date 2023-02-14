package org.devocative.thallo.fabric.gateway.scanner;

import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.Type;

public class GeneralTypeReference<T> extends TypeReference<T> {
	private final Type type;

	public GeneralTypeReference(Type type) {
		this.type = type;
	}

	@Override
	public Type getType() {
		return type;
	}
}
