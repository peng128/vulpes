package net.peng.vulpes.runtime.struct.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of ArrowSegment.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/10
 */
@AllArgsConstructor
@NoArgsConstructor
public class ArrowSegment implements Segment<List<VectorSchemaRoot>> {

  private List<VectorSchemaRoot> vectorSchemaRoots;

  @Override
  public List<VectorSchemaRoot> get() {
    return vectorSchemaRoots;
  }

  @Override
  public void put(List<VectorSchemaRoot> vectorSchemaRoots) {
    this.vectorSchemaRoots = vectorSchemaRoots;
  }
}
