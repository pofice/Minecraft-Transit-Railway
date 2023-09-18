package org.mtr.mod.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.mapping.mapper.GraphicsHolder;

import java.util.function.Consumer;

public class StoredMatrixTransformations {

	private final ObjectArrayList<Consumer<GraphicsHolder>> transformations = new ObjectArrayList<>();

	public void add(Consumer<GraphicsHolder> transformation) {
		transformations.add(transformation);
	}

	public void transform(GraphicsHolder graphicsHolder) {
		graphicsHolder.push();
		transformations.forEach(transformation -> transformation.accept(graphicsHolder));
	}

	public StoredMatrixTransformations copy() {
		final StoredMatrixTransformations storedMatrixTransformations = new StoredMatrixTransformations();
		storedMatrixTransformations.transformations.addAll(transformations);
		return storedMatrixTransformations;
	}
}