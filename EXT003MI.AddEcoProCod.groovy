/**
 * README
 * Création enregistrement dans la table CECOPC (programme ENS020).
 *
 * Name: AddEcoProCod
 * Description: 
 * Date       Changed By                     Description
 * 20230509   François Leprévost             Création verbe add pour ENS020
 */
public class AddEcoProCod extends ExtendM3Transaction {
  private final MIAPI mi;  private final DatabaseAPI database;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  private final LoggerAPI logger;
  
  public AddEcoProCod(MIAPI mi, DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility, LoggerAPI logger) {
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
    String ecoc = mi.inData.get("ECOC").trim();
    String ecop = mi.inData.get("ECOP").trim();
    String tx40 = mi.inData.get("TX40").trim();
    String tx15 = mi.inData.get("TX15").trim();
    String csno = mi.inData.get("CSNO").trim();
    String isdf = mi.inData.get("ISDF");
    
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
    
    if (ecoc.isEmpty()) {
      mi.error("Le code éco-participation est obligatoire.");
      return;
    } else if (!checkEcoContributionCodeExist(ecrg, csor, ecoc)) {
      mi.error("Le code éco-participation n'existe pas.");
      return;
    }
    
    if (ecop.isEmpty()) {
      mi.error("Le code éco-produit est obligatoire.");
      return;
    }
    
    if (tx40.isEmpty()) {
      mi.error("La description est obligatoire.");
      return;
    }
    
    if (tx15.isEmpty()) {
      mi.error("Le nom est obligatoire.");
      return;
    }
    
    if (!csno.isEmpty() && !checkCustomStatisticalNumberExist(csno)) {
      mi.error("Le code statistique douanier n'existe pas.");
      return;
    }
    
    if (!isdf.equals("0") && !isdf.equals("1")) {
      mi.error("Le code par défaut doit être égal à 0 ou 1.");
      return;
    }
    
    int isdfInt = Integer.parseInt(isdf);
    if (!createEnreg(ecrg, csor, ecoc, ecop, tx40, tx15, csno, isdfInt)) {
      mi.error("L'enregistrement existe déjà !");
      return;
    }
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
  
  // On vérifie que l'éco-organisme saisi existe.
  private boolean checkEcoOrganismExist(String ecrg, String csor) {
    DBAction query = database.table("CECORG").index("00").selection("ECSUNO").build();
    DBContainer container = query.getContainer();
    container.set("ECCONO", cono);
    container.set("ECECRG", ecrg);
    container.set("ECCSOR", csor);
    
    return query.read(container);
  }
  
  // On vérifie que le code éco-participation existe.
  private boolean checkEcoContributionCodeExist(String ecrg, String csor, String ecoc) {
    DBAction query = database.table("CECOCC").index("00").selection("CETX40").build();
    DBContainer container = query.getContainer();
    container.set("CECONO", cono);
    container.set("CEECRG", ecrg);
    container.set("CECSOR", csor);
    container.set("CEECOC", ecoc);
    
    return query.read(container);
  }
  
  // On vérifie que le code statistique douanier existe.
  private boolean checkCustomStatisticalNumberExist(String csno) {
    DBAction query = database.table("CSYCSN").index("00").selection("CKTX40").build();
    DBContainer container = query.getContainer();
    container.set("CKCONO", cono);
    container.set("CKCSNO", csno);
    
    return query.read(container);
  }
  
  // On crée l'enregistrement dans CECOPC uniquement s'il n'existe pas.
  private boolean createEnreg(String ecrg, String csor, String ecoc, String ecop, String tx40, String tx15, String csno, int isdf) {
    DBAction query = database.table("CECOPC").index("00").selection("CGTX40").build();
    DBContainer container = query.getContainer();
    
    // Champs de la clé
    container.set("CGCONO", cono);
    container.set("CGECRG", ecrg);
    container.set("CGCSOR", csor);
    container.set("CGECOP", ecop);
    
    // On recherche si l'enregistrement existe
    if (!query.read(container)) {
      // Alimentation des autres champs
      container.set("CGECOC", ecoc);
      container.set("CGTX40", tx40);
      container.set("CGTX15", tx15);
      container.set("CGISDF", isdf);
      container.set("CGCSNO", csno);
      container.set("CGRGDT", utility.call("DateUtil","currentDateY8AsInt")); // Date du jour au format YMD8
      container.set("CGRGTM", utility.call("DateUtil","currentTimeAsInt"));   // Heure du jour
      container.set("CGLMDT", utility.call("DateUtil","currentDateY8AsInt")); // Date du jour au format YMD8
      container.set("CGCHNO", 1);                                             // Incrément de modification initialisé à 1 
      container.set("CGCHID", chid);
      
      query.insert(container);
      return true;
    } else {
      return false;
    }
  }
  
}





















