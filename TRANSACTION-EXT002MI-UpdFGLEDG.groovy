/**
 * README
 * This Transaction is being triggered by EXT002MI
 *
 * Name: EXT002MI-UpdFGLEDG
 * Description: Update fields RECO and REDE in database FGLEDG
 * Date       Changed By                     Description
 * 20230404   Ludovic Travers                Create Transaction EXT002MI-UpdFGLEDG
 */
 
public class UpdFGLEDG extends ExtendM3Transaction {
  private final MIAPI mi
  private final ProgramAPI program
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  public UpdFGLEDG(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
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
    
    // Select fields to handle from table FGLEDG
    DBAction query = database.table("FGLEDG")
      .index("00")
    .selection("EGCONO", "EGDIVI", "EGYEA4", "EGJRNO", "EGJSNO", "EGRECO", "EGREDE", "EGLMDT", "EGCHNO", "EGCHID")
      .build()
    
    DBContainer container = query.getContainer()
    
    // Set the key fields of the record to get
    container.set("EGCONO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("CONO"))) // int
    container.set("EGDIVI", mi.inData.get("DIVI").trim())
    container.set("EGYEA4", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("YEA4"))) // int
    container.set("EGJRNO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("JRNO"))) // int
    container.set("EGJSNO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("JSNO"))) // int
    
    Closure<?> updateCallBack = { LockedResult lockedResult ->

      if (!mi.inData.get("RECO").isEmpty()) {
        lockedResult.set("EGRECO", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("RECO")))    // To cast from String to Int
      }
      
      if (!mi.inData.get("REDE").isEmpty()) {
        lockedResult.set("EGREDE", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("REDE")))    // To cast from String to Int
      }

      // Audit Fields
   
      // The change number is retrieved from the existing record and is being added 1
      chno = lockedResult.get("EGCHNO")
      chno++
      lockedResult.set("EGLMDT", utility.call("DateUtil","currentDateY8AsInt"))   // Today's date in YMD8 format
      lockedResult.set("EGCHNO", chno)                                            // Change increment incremented by 1
      lockedResult.set("EGCHID", chid)                                            // Profile of the user who modified the record
      
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