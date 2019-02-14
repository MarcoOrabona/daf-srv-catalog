package it.gov.daf.catalogmanager.utils

import com.typesafe.config.ConfigFactory
import it.gov.daf.catalogmanager.utils.datastructures.DatasetType
import it.gov.daf.model.MetaCatalog
import play.api.Logger

import scala.util.{Failure, Success, Try}

case class UriDataset(
                       domain: String = "NO_DOMAIN",
                       typeDs: DatasetType.Value = DatasetType.RAW,
                       groupOwn: String = "NO_groupOwn",
                       owner: String = "NO_owner",
                       theme: String = "NO_theme",
                       subtheme :String = "NO_theme",
                       nameDs: String = "NO_nameDs") {

  val config = ConfigFactory.load()

  def getUri(): String = {
    domain + "://" + "dataset/" + typeDs + "/" + groupOwn + "/" + owner + "/" + theme + "/" + subtheme + "/" + nameDs
  }


  def getUrl(): String = {

    val basePath = config.getString("Inj-properties.hdfsBasePath")
    val baseDataPath = config.getString("Inj-properties.dataBasePath")
    typeDs match {
      case DatasetType.STANDARD => basePath + baseDataPath + "/" + typeDs + "/" + theme + "/" + subtheme + "/" + groupOwn + "/" + nameDs
      case DatasetType.ORDINARY => basePath + baseDataPath + "/" + typeDs + "/" + owner + "/" + theme + "/" + subtheme + "/" + groupOwn + "/" + nameDs
      case DatasetType.RAW => basePath + baseDataPath + "/" + typeDs + "/" + owner + "/" + theme + "/" + subtheme + "/" + groupOwn + "/" + nameDs
      case _ => "-1"
    }
  }
}


object UriDataset  {
  def apply(uri: String): UriDataset = {
    Try {
      val uri2split = uri.split("://")
      val uriParts = uri2split(1).split("/")
      new UriDataset(
        domain = uri2split(0),
        typeDs = DatasetType.withNameOpt(uriParts(1)).get,
        groupOwn = uriParts(2),
        owner = uriParts(3),
        theme = uriParts(4),
        subtheme = uriParts(5),
        nameDs = uriParts(6))
    } match {
      case Success(s) => s
      case Failure(err) =>
        Logger.error("Error while creating uri: " + uri + " - " + err.getMessage)
        UriDataset()
    }

  }

  def convertToUriDataset(schema: MetaCatalog): UriDataset =  {

      val typeDs = if (schema.operational.is_std)
        DatasetType.STANDARD
      else
        DatasetType.ORDINARY
      new UriDataset(
        domain = "daf",
        typeDs = typeDs,
        groupOwn = schema.operational.group_own,
        owner = schema.dcatapit.owner_org.get,
        theme  = schema.operational.theme,
        subtheme = schema.operational.subtheme,
        nameDs = schema.dataschema.avro.name
      )

  }

}