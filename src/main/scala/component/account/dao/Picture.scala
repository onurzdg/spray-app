package component.account.dao

import component.account.domain.{Email, Picture}
import storage.postgres.dbSimple._


private[account]
class PicturesTable(tag: Tag) extends Table[Picture](tag, "picture") {    //Slick Table
def id = column[Option[Long]]("pic_id", O.PrimaryKey, O.AutoInc)
  def url = column[String]("pic_url")
  def accountId = column[Long]("account_id")
  def * = (id, url, accountId) <> ((Picture.apply _).tupled, Picture.unapply)

  // A reified foreign key relation that can be navigated to create a join
  def account = foreignKey("account_FK", accountId, TableQuery[AccountTable])(_.id)
  def idx = index("idx_acc_id", (id, accountId), unique = true)
}

private[account]
class Pictures  {
  import storage.operationSuccessMapper

  val accounts = TableQuery[AccountTable]
  val pictures = TableQuery[PicturesTable]

  private val picturesAutoInc = pictures returning pictures.map(_.id) into { case (p, id) => p.copy(id = id)}

  // sample queries,some of them are not used actually

  private val qRetrievePictureById = Compiled( (id: Column[Long]) =>
    for {pic <- pictures if pic.id === id} yield pic )

  private val qRetrievePictureUrlById = Compiled( (id: Column[Long]) =>
    for {pic <- pictures if pic.id === id} yield pic.url )

  private def qRetrievePicturesWithEmailInnerJoin =
    for {
      (pic, acc) <- pictures innerJoin accounts on (_.accountId === _.id)
    } yield (pic.url, acc.email)


  private def qRetrievePicturesWithEmailLeftOuterJoin =
    for {
      (pic, acc) <- pictures leftJoin accounts on (_.accountId === _.id)
    } yield (pic.url, acc.email)

  private val qRetrievePicturesWithEmailRightOuterJoin = Compiled( (accountId: Column[Long]) =>
    for {
      (pic, acc) <- pictures rightJoin accounts on (_.accountId === _.id) if acc.id === accountId if pic.id =!= 21L
    } yield (pic.url, acc.email) )

  private def qRetrievePicturesWithEmailFullOuterJoin =
    for {
      (pic, acc) <- pictures outerJoin accounts on (_.accountId === _.id)
    } yield (pic.url, acc.email)

  private val qRetrievePicsByAccountId = Compiled( (accountId: Column[Long]) =>
     for {
      pic <- pictures if pic.accountId === accountId
    } yield pic)

  private val qRetrievePicUrlsAccountId = Compiled( (accountId: Column[Long]) =>
    for {
      pic <- pictures if pic.accountId === accountId
    } yield pic.url )

  def retrievePicsByAccountId(accountId: Long)(implicit session: Session): List[String] =
    qRetrievePicUrlsAccountId(accountId).list


  case class Pic(url: String, email: Email)
  def retrievePicturesWithEmail(implicit session: Session) =
    qRetrievePicturesWithEmailInnerJoin.mapResult{ case (url, email) => Pic(url, email)}.list

  def retrievePictureById(id: Long)(implicit session: Session): Option[Picture] = {
    qRetrievePictureById(id).firstOption
  }

  def updatePicture(picture: Picture)(implicit session: Session): Boolean =
      operationSuccessMapper(pictures.filter(_.id === picture.id.get).update(picture))


  def createPicture(picture: Picture)(implicit session: Session): Picture =
    picturesAutoInc.insert(picture)

  def updatePictureUrl(pictureId: Int, url: String)(implicit session: Session): Boolean =
    operationSuccessMapper(qRetrievePictureUrlById(pictureId).update(url))

  def deletePicture(pictureId: Int)(implicit s: Session): Boolean =
    operationSuccessMapper(qRetrievePictureById(pictureId).delete)

  def deletePicturesByAccountId(accountId: Long)(implicit s: Session): Boolean =
    operationSuccessMapper(qRetrievePicsByAccountId(accountId).delete)
}
