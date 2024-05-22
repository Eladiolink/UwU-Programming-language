class NaryTreeNode<T>(val value: T) {
  val children: MutableList<NaryTreeNode<T>> = mutableListOf()

  fun addChild(child: NaryTreeNode<T>) {
    children.add(child)
  }

  fun removeChild(child: NaryTreeNode<T>): Boolean {
    return children.remove(child)
  }
}

class NaryTree<T>(val root: NaryTreeNode<T>) {
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
