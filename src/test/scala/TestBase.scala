import module.Bindings
import scaldi.Injector

trait TestBase {
  implicit val inj: Injector = Bindings.appInjector
  def removeSpaceAndControlChars(str: String) = str.filter(_ >= ' ').trim().replaceAll(" +", "")
}
