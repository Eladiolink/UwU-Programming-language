
package compiler.naryTree

data class NaryTreeNode<T>(val value: T, val children: List<NaryTreeNode<T>>) {
  override fun toString(): String {
      return "NaryTree(" + value.toString() + ", children=" + children.toString() + ")"

  }
}

data class NaryTree<T>(val root: NaryTreeNode<T>) {
  fun find(value: T): NaryTreeNode<T>? {
    return findRecursive(root, value)
  }

  private fun findRecursive(node: NaryTreeNode<T>?, value: T): NaryTreeNode<T>? {
    if (node == null) return null
    if (node.value == value) return node
    for (child in node.children) {
      val result = findRecursive(child, value)
      if (result != null) return result
    }
    return null
  }
}
