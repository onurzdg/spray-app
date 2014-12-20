package api

import component.account.AccountService

private[api]
trait CommonServices {self: CommonTraits =>
  val accountService = inject[AccountService]
}
