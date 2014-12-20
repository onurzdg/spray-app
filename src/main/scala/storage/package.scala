package object storage {
  def operationSuccessMapper: Int => Boolean = {case 0 => false case _ => true}
}
