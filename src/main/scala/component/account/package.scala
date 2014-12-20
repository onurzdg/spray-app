package component

import component.account.dao.{Accounts, Pictures}
import scaldi.Module



package object account {
  class AccountModule extends Module {
    binding to new Accounts
    binding to new Pictures
    binding to injected[AccountService]
  }
}
