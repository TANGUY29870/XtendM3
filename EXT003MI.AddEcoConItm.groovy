/**
 * README
 * Création enregistrement dans la table CECOCI (programme ENS025).
 *
 * Name: AddEcoConItm
 * Description: 
 * Date       Changed By                     Description
 * 20230510   François Leprévost             Création verbe add pour ENS025
 */
public class AddEcoConItm extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  private final LoggerAPI logger;
  
  public AddEcoConItm(MIAPI mi, DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility, LoggerAPI logger) {
    this.mi = mi;
    this.database = database;
    this.program = program;
    this.logger = logger;
    this.utility = utility;
  }
  
  int cono = 0;
  String chid = "";
  
  public void main() {
    cono = (Integer) program.getLDAZD().CONO;
    chid = program.getUser();
    String ecrg = mi.inData.get("ECRG").trim();
    String csor = mi.inData.get("CSOR").trim();
    String ecop = mi.inData.get("ECOP").trim();
    String itno = mi.inData.get("ITNO").trim();
    String vfdt = mi.inData.get("VFDT");
    String vtdt = mi.inData.get("VTDT");
    
    if (ecrg.isEmpty()) {
      mi.error("L'éco-organisme est obligatoire.");
      return;
    }
    
    if (csor.isEmpty()) {
      mi.error("Le pays est obligatoire.");
      return;
    } else if (!checkCountryExist(csor)) {
      mi.error("Pays inexistant.");
      return;
    } 
    
    if (!checkEcoOrganismExist(ecrg, csor)) {
      mi.error("Eco-organisme inexistant.");
      return;
    }
    
    if (ecop.isEmpty()) {
      mi.error("Le code eco-produit est obligatoire.");
      return;
    } else if (!checkEcoProductCodeExist(ecrg, csor, ecop)) {
      mi.error("Code éco-production inexistant.");
      return
    }
    
    if (itno.isEmpty()) {
      mi.error("Le code article est obligatoire.");
      return;
    } else if (!checkItemExist(itno)) {
      mi.error("Code article inexistant.");
      return;
    }
    
    int vfdtInt = 0;
    if (vfdt.isEmpty()) {
      mi.error("La date de début de validité est obligatoire.");
      return;
    } else {
      // On vérifie que la date de début de validité est correctement saisie
      if (!utility.call("DateUtil", "isDateValid", vfdt, "yyyyMMdd")) {
        mi.error("La date de début de validité est invalide.");
        return;
      } else {
        vfdtInt = utility.call("NumberUtil","parseStringToInteger", vfdt);
      }
    }
    
    int vtdtInt = 0;
    if (!vtdt.isEmpty()) {
  		// On vérifie que la date de fin de validité est correctement saisie
  		if (!vtdt.equals("0") && !utility.call("DateUtil", "isDateValid", vtdt, "yyyyMMdd")) {
  		  mi.error("La date de fin de validité est invalide.");
  		  return;
  		} else {
  			 vtdtInt = utility.call("NumberUtil","parseStringToInteger", vtdt);
  			 // On vérifie que la date de fin de validité est supérieure ou égale à la date de début de validité
  			 if (vtdtInt != 0 && (vtdtInt < vfdtInt)) {
  				mi.error("La date de fin de validité doit être supérieure ou égale à la date de début.");
  				return;
  			}
  		}
  	}
  	
  	if (!createEnreg(ecrg, csor, ecop, itno, vfdtInt, vtdtInt)) {
      mi.error("L'enregistrement existe déjà !");
      return;
    }
  }

  // On vérifie que l'éco-organisme saisi existe.
  private boolean checkEcoOrganismExist(String ecrg, String csor) {
    DBAction query = database.table("CECORG").index("00").selection("ECSUNO").build();
    DBContainer container = query.getContainer();
    container.set("ECCONO", cono);
    container.set("ECECRG", ecrg);
    container.set("ECCSOR", csor);
    
    return query.read(container);
  }
  
    // On vérifie que le pays existe.
  private boolean checkCountryExist(String csor) {
    DBAction query = database.table("CSYTAB").index("00").selection("CTTX40").build();
    DBContainer container = query.getContainer();
    container.set("CTCONO", cono);
    container.set("CTDIVI", "");
    container.set("CTSTCO", "CSCD");
    container.set("CTSTKY", csor);
    container.set("CTLNCD", "");
    
    return query.read(container);
  }
  
  // On vérifie que le code éco-production existe.
  private boolean checkEcoProductCodeExist(String ecrg, String csor, String ecop) {
    DBAction query = database.table("CECOPC").index("00").selection("CGTX40").build();
    DBContainer container = query.getContainer();
    container.set("CGCONO", cono);
    container.set("CGECRG", ecrg);
    container.set("CGCSOR", csor);
    container.set("CGECOP", ecop);
    
    return query.read(container);
  }
  
  // On vérifie que le code article existe.
  private boolean checkItemExist(String itno) {
    DBAction query = database.table("MITMAS").index("00").selection("MMITDS").build();
    DBContainer container = query.getContainer();
    container.set("MMCONO", cono);
    container.set("MMITNO", itno);
    
    return query.read(container);
  }
  
  // On crée l'enregistrement dans CECOCI uniquement s'il n'existe pas.
  private boolean createEnreg(String ecrg, String csor, String ecop, String itno, int vfdtInt, int vtdtInt) {
    DBAction query = database.table("CECOCI").index("00").selection("CIRGDT").build();
    DBContainer container = query.getContainer();
    
    // Champs de la clé
    container.set("CICONO", cono);
    container.set("CIECRG", ecrg);
    container.set("CICSOR", csor);
    container.set("CIECOP", ecop);
    container.set("CIITNO", itno);
    
     // On recherche si l'enregistrement existe
    if (!query.read(container)) {
      // Alimentation des autres champs
      container.set("CIVFDT", vfdtInt);
      container.set("CIVTDT", vtdtInt);
      container.set("CIRGDT", utility.call("DateUtil","currentDateY8AsInt")); // Date du jour au format YMD8
      container.set("CIRGTM", utility.call("DateUtil","currentTimeAsInt"));   // Heure du jour
      container.set("CILMDT", utility.call("DateUtil","currentDateY8AsInt")); // Date du jour au format YMD8
      container.set("CICHNO", 1);                                             // Incrément de modification initialisé à 1 
      container.set("CICHID", chid);
      
      query.insert(container);
      return true;
    } else {
      return false;
    }
  }
}






















