/**
 * README
 * This Transaction is being triggered by EXT002MI
 *
 * Name: EXT002MI-UpdFSLEDG
 * Description: Update fields RECO and REDE in database FSLEDG
 * Date       Changed By                     Description
 * 20230404   Ludovic Travers                Create Transaction EXT002MI-UpdFSLEDG
 */
 
public class UpdFSLEDG extends ExtendM3Transaction {
  private final MIAPI mi
  private final ProgramAPI program
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  public UpdFSLEDG(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi
    this.program = program
    this.database = database
    this.logger = logger
    this.utility = utility
  }
  
  // Declaration of specific variables
  String chid
  int chno
  
  public void main() {
    chid = program.getUser()
    
    // Select fields to handle from table FSLEDG
    DBAction query = database.table("FSLEDG")
      .index("00")
    .selection("ESCONO", "ESDIVI", "ESYEA4", "ESJRNO", "ESJSNO", "ESCINO", "ESPYNO", "ESRECO", "ESREDE", "ESRGDT", "ESRGTM", "ESLMDT", "ESCHNO", "ESCHID", "ESLMTS")
      .build()
    
    DBContainer container = query.getContainer()
   
    // Set the key fields of the record to get
    container.set("ESCONO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("CONO"))) // int
    container.set("ESDIVI", mi.inData.get("DIVI").trim())
    container.set("ESYEA4", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("YEA4"))) // int
    container.set("ESJRNO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("JRNO"))) // int
    container.set("ESJSNO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("JSNO"))) // int
    
    Closure<?> updateCallBack = { LockedResult lockedResult ->

      if (!mi.inData.get("RECO").isEmpty()) {
        lockedResult.set("ESRECO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("RECO")))    // To cast from String to Int
      }
      
      if (!mi.inData.get("REDE").isEmpty()) {
        lockedResult.set("ESREDE", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("REDE")))    // To cast from String to Int
      }

      // Audit fields
   
      // The change number is retrieved from the existing record and is being added 1
      chno = lockedResult.get("ESCHNO")
      chno++
      lockedResult.set("ESLMDT", utility.call("DateUtil","currentDateY8AsInt"))  // Today's date in YMD8 format
      lockedResult.set("ESCHNO", chno)                                           // Change increment incremented by 1
      lockedResult.set("ESCHID", chid)                                           // Profile of the user who modified the record
      
      // We never touch the LMTS field, M3 updates it automatically
      
      lockedResult.update()
    }
    
    // If there is an existing record of this key, the fields are set and the record is updated in the table
    if (query.read(container)) {
      query.readAllLock(container, 5, updateCallBack) // For each record on the found key, we lock it and we update it
    } else {
      // If the record doesn't already exist in the table, an error is thrown
      mi.error("L'enregistrement n'existe pas.")
    }
  }
}