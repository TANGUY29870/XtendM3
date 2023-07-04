/**
 * README
 * Création enregistrement dans la table CECOCC (programme ENS015).
 *
 * Name: AddEcoConCod
 * Description: 
 * Date       Changed By                     Description
 * 20230509   François Leprévost             Création verbe add pour ENS015
 */
public class AddEcoConCod extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public AddEcoConCod(MIAPI mi, DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.program = program
    this.utility = utility
  }
  
  int cono = 0
  String chid = ""
  
  public void main() {
    cono = (Integer) program.getLDAZD().CONO
    chid = program.getUser()
    String ecrg = mi.inData.get("ECRG").trim()
    String csor = mi.inData.get("CSOR").trim()
    String ecoc = mi.inData.get("ECOC").trim()
    String tx40 = mi.inData.get("TX40").trim()
    String tx15 = mi.inData.get("TX15").trim()
    String crid = mi.inData.get("CRID").trim()
    String ceid = mi.inData.get("CEID").trim()
    String flgt = mi.inData.get("FLGT").trim()
    
    if (ecrg.isEmpty()) {
      mi.error("L'éco-organisme est obligatoire.")
      return
    }
    
    if (csor.isEmpty()) {
      mi.error("Le pays est obligatoire.")
      return
    } else if (!checkCountryExist(csor)) {
      mi.error("Pays inexistant.")
      return
    } 
    
    if (!checkEcoOrganismExist(ecrg, csor)) {
      mi.error("Eco-organisme inexistant.")
      return
    }
    
    if (ecoc.isEmpty()) {
      mi.error("Le code éco-participation est obligatoire.")
      return
    }
    
    if (tx40.isEmpty()) {
      mi.error("La description est obligatoire.")
      return
    }
    
    if (tx15.isEmpty()) {
      mi.error("Le nom est obligatoire.")
      return
    }
    
    if (crid.isEmpty()) {
      mi.error("L'élément de coût est obligatoire.")
      return
    } else if (!checkChargeExist(crid)) {
      mi.error("Frais inexistant.")
      return
    } 
    
    if (ceid.isEmpty()) {
      mi.error("L'élément de coût est obligatoire.")
      return
    } else if (!checkCostingElementExist(ceid)) {
       mi.error("Centre de coût inexistant.")
      return
    }
    
    if (flgt.isEmpty()) {
      mi.error("Le texte légal est obligatoire.")
      return
    }
    
    if (!createEnreg(ecrg, csor, ecoc, tx40, tx15, crid, ceid, flgt)) {
      mi.error("L'enregistrement existe déjà !")
      return
    }
  }
  
  // On vérifie que le pays existe.
  private boolean checkCountryExist(String csor) {
    DBAction query = database.table("CSYTAB").index("00").selection("CTTX40").build()
    DBContainer container = query.getContainer()
    container.set("CTCONO", cono)
    container.set("CTDIVI", "");
    container.set("CTSTCO", "CSCD")
    container.set("CTSTKY", csor)
    container.set("CTLNCD", "")
    
    return query.read(container)
  }
  
  // On vérifie que l'éco-organisme saisi existe.
  private boolean checkEcoOrganismExist(String ecrg, String csor) {
    DBAction query = database.table("CECORG").index("00").selection("ECSUNO").build()
    DBContainer container = query.getContainer()
    container.set("ECCONO", cono)
    container.set("ECECRG", ecrg)
    container.set("ECCSOR", csor)
    
    return query.read(container)
  }
  
  // On vérifie que le frais existe.
  private boolean checkChargeExist(String crid) {
    DBAction query = database.table("OLICHA").index("00").selection("MJPC01").build()
    DBContainer container = query.getContainer()
    container.set("MJCONO", cono)
    container.set("MJCRID", crid)
    
    return query.read(container)
  }

  // On vérifie que le centre de coût existe
  private boolean checkCostingElementExist(String ceid) {
    DBAction query = database.table("MPCELE").index("00").selection("INTX30").build()
    DBContainer container = query.getContainer()
    container.set("INCONO", cono)
    container.set("INCEID", ceid)
    
    return query.read(container)
  }
  
  // On crée l'enregistrement dans CECOCC uniquement s'il n'existe pas.
  private boolean createEnreg(String ecrg, String csor, String ecoc, String tx40, String tx15, String crid, String ceid, String flgt) {
    DBAction query = database.table("CECOCC").index("00").selection("CETX40").build()
    DBContainer container = query.getContainer()
    
    // Champs de la clé
    container.set("CECONO", cono)
    container.set("CEECRG", ecrg)
    container.set("CECSOR", csor)
    container.set("CEECOC", ecoc)
    
    // On recherche si l'enregistrement existe
    if (!query.read(container)) {
      // Alimentation des autres champs
      container.set("CETX40", tx40)
      container.set("CETX15", tx15)
      container.set("CECRID", crid);
      container.set("CECEID", ceid)
      container.set("CEFLGT", flgt)
      container.set("CERGDT", utility.call("DateUtil","currentDateY8AsInt"))  // Date du jour au format YMD8
      container.set("CERGTM", utility.call("DateUtil","currentTimeAsInt"))    // Heure du jour
      container.set("CELMDT", utility.call("DateUtil","currentDateY8AsInt"))  // Date du jour au format YMD8
      container.set("CECHNO", 1)
      container.set("CECHID", chid)
      
      query.insert(container)
      return true
    } else {
      return false
    }
  }
}




















