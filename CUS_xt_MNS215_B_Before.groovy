/**
 * README
 * This extension is being triggered by MNS215
 *
 * Name: CUS_xt_MNS215_B_Before
 * Description: Adds customer/supplier related mail addresses to MNS215
 * Date       Changed By                     Description
 * 20220301   Ludovic Travers                Create CUS_xt_MNS215_B_Before extension
 * 20220525   Eric Masson                    Fix post review issues
 * 20230316   Ludovic Travers                Gestion de l'édition des devis QPS601PF
 */

public class CUS_xt_MNS215_B_Before extends ExtendM3Trigger {

  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller


  public CUS_xt_MNS215_B_Before(ProgramAPI program, InteractiveAPI interactive,
                                DatabaseAPI database, LoggerAPI logger, MICallerAPI miCaller) {
    this.program = program
    this.interactive = interactive
    this.database = database
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    def CM101 = program.getTableRecord("CCM101")
    int CONO = CM101.FXCONO
    def CM110 = program.getTableRecord("CCM110")
    String BJNO = CM110.DPBJNO
    String PRTF = CM110.DPPRTF
    String QCMD = ""

    QCMD = callGetCJBCMD_withDatabase(BJNO, "01")
    switch (PRTF.trim()) {
      case "OIS606PF":
        retrieveInfos(CONO, BJNO, PRTF, QCMD)
        break
      case "QPS601PF":
        retrieveInfos(CONO, BJNO, PRTF, QCMD)
        break
      case "OIS199PF":
        retrieveInfos(CONO, BJNO, PRTF, QCMD)
        break
      case "PPS601PF":
        retrieveInfos(CONO, BJNO, PRTF, QCMD)
        break
      case "PPS821PF":
        retrieveMPCLAH(CONO, BJNO, PRTF, QCMD)
        break
      case "MMS480PF":
        int DLIX = program.getLDAZZ().DLIX
        String oCONA = this.callMHDISH_withDatabase(CONO, DLIX)
        this.callCMS055MI_LstByCustomer(BJNO, PRTF, oCONA, "06", "")
        break
      case "MWS620PF":
        int DLIX = program.getLDAZZ().DLIX
        String oCONA = this.callMHDISH_withDatabase(CONO, DLIX)
        this.callCMS055MI_LstByCustomer(BJNO, PRTF, oCONA, "06", "")
        break
      default:
        break
    }
  }

  /**
   * Retrieve table name stored in OPNQRY from CJBCMD
   */
  private String callGetCJBCMD_withDatabase(String pBJNO, String pBJLI) {
    String QCMD = ""

    DBAction query = database.table("CJBCMD")
      .index("00")
      .selection("CMQCMD")
      .build()
    DBContainer CJBCMD = query.getContainer()
    CJBCMD.set("CMBJNO", pBJNO)
    CJBCMD.set("CMBJLI", pBJLI)
    Closure<?> resultHandler = { DBContainer data ->
      QCMD = data.get("CMQCMD")
    }
    query.readAll(CJBCMD, 2, resultHandler)

    return QCMD
  }

  /**
   * Retrieve informations to get the contacts
   */
  private void retrieveInfos(int pCONO, String pBJNO, String pPRTF, String pQCMD) {
    int pos = 0
    int posfin = 0
    String oCUNO = ""
    String oPYNO = ""
    String oSUNO = ""
    String ORNO = ""
    String PUNO = ""
    String IVNO = ""
    String DIVI = ""
    String YEA4 = ""
    String INPX = ""

    pos = pQCMD.indexOf("ORNO")
    posfin = pQCMD.indexOf("'", pos + 10)
    ORNO = pQCMD.substring(pos + 7, posfin)
    
    if (pPRTF.trim() == "QPS601PF") {
        pos = pQCMD.indexOf("QFORNO = N'") + 11
        ORNO = pQCMD.substring(pos, pos + 10)   // l'ORNO a une longueur de 10 caractères
    }
    
    if (pos != -1) {
      oCUNO = this.callOOHEAD_withDatabase(pCONO, pBJNO, pPRTF, ORNO)
      this.callCMS055MI_LstByCustomer(pBJNO, pPRTF, oCUNO, "06", "")

    } else {
      pos = pQCMD.indexOf("PUNO=")
      if (pos != -1) {
        posfin = pQCMD.indexOf("'", pos + 10)
        PUNO = pQCMD.substring(pos + 7, posfin)
      } else {
        pos = pQCMD.indexOf("PUNO =")
        posfin = pQCMD.indexOf("'", pos + 12)
        PUNO = pQCMD.substring(pos + 9, posfin)
      }

      if (pos != -1) {
        oSUNO = this.callMPHEAD_withDatabase(pCONO, PUNO)
        this.callCRS620MI_LstSupplierRef(pBJNO, pPRTF, oSUNO, "10")
      } else {
        pos = pQCMD.indexOf("EXIN=")
        if (pos != -1) {
          posfin = pQCMD.indexOf("'", pos + 10)
          IVNO = pQCMD.substring(pos + 7, posfin)
        } else {
          pos = pQCMD.indexOf("EXIN =")
          posfin = pQCMD.indexOf("'", pos + 12)
          IVNO = pQCMD.substring(pos + 9, posfin)
        }

        if (IVNO.length() > 9) {
          String onlyIVNO = extractInts(IVNO)
          INPX = IVNO.replaceAll("[\\d.]", "")
          IVNO = onlyIVNO
        }

        pos = pQCMD.indexOf("DIVI")
        posfin = pQCMD.indexOf("'", pos + 8)
        DIVI = pQCMD.substring(pos + 7, posfin)
        pos = pQCMD.indexOf("YEA4")
        YEA4 = pQCMD.substring(pos + 5, pos + 9)
        if (pos != -1) {
          oPYNO = this.callOIS350MI_GetInvHead(pCONO, DIVI, YEA4, IVNO, INPX)
          this.callCMS055MI_LstByCustomer(pBJNO, pPRTF, oPYNO, "03", "FACTURE")
        }
      }
    }
  }

  /**
   * Remove alpha caracters
   */
  def extractInts(String input) {
    input.findAll(/\d+/).first()
  }

  /**
   * Retrieve informations from MPCLAH to get contacts
   */
  private void retrieveMPCLAH(int pCONO, String pBJNO, String pPRTF, String pQCMD) {
    int pos = 0
    int posfin = 0
    String oSUNO = ""
    String CLAN = ""

    pos = pQCMD.indexOf("CLAN")
    posfin = pQCMD.indexOf("'", pos + 10)
    CLAN = pQCMD.substring(pos + 7, posfin)
    if (pos != -1) {
      oSUNO = this.callMPCLAH_withDatabase(pCONO, CLAN)
      this.callCRS620MI_LstSupplierRef(pBJNO, pPRTF, oSUNO, "10")
    }
  }

  /**
   * Retrieve customer from OOHEAD
   */
  private String callOOHEAD_withDatabase(int pCONO, String pBJNO, String pPRTF, String pORNO) {
    String CUNO = ""

    DBAction query = database.table("OOHEAD")
      .index("00")
      .selection("OACUNO")
      .build()
    DBContainer OOHEAD = query.getContainer()
    OOHEAD.set("OACONO", pCONO)
    OOHEAD.set("OAORNO", pORNO)
    Closure<?> resultHandler = { DBContainer data ->
      CUNO = data.get("OACUNO")
    }
    query.readAll(OOHEAD, 2, resultHandler)

    return CUNO.trim()
  }

  /**
   * Retrieve payer from OINVOH
   */
  private String callOINVOH_withDatabase(int pCONO, String pDIVI, int pYEA4, String pIVNO) {
    String PYNO = ""
    String iINPX = ""

    DBAction query = database.table("OINVOH")
      .index("00")
      .selection("OACUNO")
      .build()
    DBContainer OINVOH = query.getContainer()
    OINVOH.set("UHCONO", pCONO)
    OINVOH.set("UHDIVI", pDIVI)
    OINVOH.set("UHYEA4", pYEA4)
    OINVOH.set("UHINPX", iINPX)
    OINVOH.set("UHIVNO", pIVNO)
    Closure<?> resultHandler = { DBContainer data ->
      PYNO = data.get("UHPYNO")
    }
    query.readAll(OINVOH, 5, resultHandler)
    return PYNO.trim()
  }

  /**
   * Retrieve supplier from MPCLAH
   */
  private String callMPCLAH_withDatabase(int pCONO, String pCLAN) {
    String SUNO = ""

    DBAction query = database.table("MPCLAH")
      .index("00")
      .selection("CHSUNO")
      .build()
    DBContainer MPCLAH = query.getContainer()
    MPCLAH.set("CHCONO", pCONO)
    MPCLAH.set("CHCLAN", pCLAN)
    Closure<?> resultHandler = { DBContainer data ->
      SUNO = data.get("CHSUNO")
    }
    query.readAll(MPCLAH, 2, resultHandler)
    return SUNO.trim()
  }

  /**
   * Retrieve supplier from MPHEAD
   */
  private String callMPHEAD_withDatabase(int pCONO, String pPUNO) {
    String SUNO = ""

    DBAction query = database.table("MPHEAD")
      .index("00")
      .selection("IASUNO")
      .build()
    DBContainer MPHEAD = query.getContainer()
    MPHEAD.set("IACONO", pCONO)
    MPHEAD.set("IAPUNO", pPUNO)
    Closure<?> resultHandler = { DBContainer data ->
      SUNO = data.get("IASUNO")
    }
    query.readAll(MPHEAD, 2, resultHandler)
    return SUNO.trim()
  }

  /**
   * Retrieve delivered customer from MHDISH
   */
  private String callMHDISH_withDatabase(int pCONO, int pDLIX) {
    String CONA = ""

    DBAction query = database.table("MHDISH")
      .index("00")
      .selection("OQCONA")
      .build()
    DBContainer MHDISH = query.getContainer()
    MHDISH.set("OQCONO", pCONO)
    MHDISH.set("OQINOU", 1)
    MHDISH.set("OQDLIX", pDLIX)
    Closure<?> resultHandler = { DBContainer data ->
      CONA = data.get("OQCONA")
    }
    query.readAll(MHDISH, 3, resultHandler)
    return CONA.trim()
  }

  /**
   * Call OIS350MI_GetInvHead
   */
  private String callOIS350MI_GetInvHead(int pCONO, String pDIVI, String pYEA4, String pIVNO, String pINPX) {
    String PYNO = ""
    def params = ["CONO": "" + pCONO, "DIVI": pDIVI, "YEA4": pYEA4, "IVNO": pIVNO, "INPX": pINPX]

    def callback = {
      Map<String, String> response ->
        if (response.PYNO != null) {
          PYNO = response.PYNO.trim()
        }
    }
    miCaller.call("OIS350MI", "GetInvHead", params, callback)
    return PYNO
  }

  /**
   * Call CMS055MI_LstByCustomer
   */
  private void callCMS055MI_LstByCustomer(String pBJNO, String pPRTF, String pCUNO, String pADRT, String pRFTP) {
    String CNPE = ""
    String iADID = ""
    String iNFTR = "2"
    int indCNPE = 0
    ArrayList<String> tabCNPE = new ArrayList<String>()
    HashMap<String, String> tabCNPE_EMAL = new HashMap<String, String>()

    def params = ["CUNO": pCUNO, "ADRT": pADRT, "ADID": iADID, "RFTP": pRFTP, "NFTR": iNFTR]

    def callback = {
      Map<String, String> response ->
        if ((response.CNPE != null) && (response.CUNO.trim().equals(pCUNO.trim()))) {
          CNPE = response.CNPE.trim()
          tabCNPE.push(CNPE)
          indCNPE++
        }
    }
    miCaller.call("CMS055MI", "LstByCustomer", params, callback)

    String pEMAL = ""
    for (int i = 0; i < indCNPE; i++) {
      tabCNPE_EMAL.put(tabCNPE[i], this.callCRS618MI_Get(pBJNO, pPRTF, tabCNPE[i]))
    }

    for (String i : tabCNPE_EMAL.keySet()) {
      this.callAddMedia(pBJNO, pPRTF, tabCNPE_EMAL.get(i))
    }

  }

  /**
   * Call CRS618MI_Get
   */
  private String callCRS618MI_Get(String pBJNO, String pPRTF, String pCNPE) {
    String EMAL = ""
    def params = ["CNPE": pCNPE]

    def callback = {
      Map<String, String> response ->
        if (response.EMAL != null) {
          EMAL = response.EMAL.trim()
        }
    }
    miCaller.call("CRS618MI", "Get", params, callback)

    return EMAL
  }

  /**
   * Call CRS620MI_LstSupplierRef
   */
  private void callCRS620MI_LstSupplierRef(String pBJNO, String pPRTF, String pSUNO, String pRFTY) {
    String EMAL = ""
    int indEMAL = 0
    ArrayList<String> tabEMAL = new ArrayList<String>()
    def params = ["SUNO": pSUNO, "RFTY": pRFTY]

    def callback = {
      Map<String, String> response ->
        if (response.EMAL != null) {
          EMAL = response.EMAL.trim()
          tabEMAL.push(EMAL)
          indEMAL++
        }
    }
    miCaller.call("CRS620MI", "LstSupplierRef", params, callback)

    for (int i = 0; i < indEMAL; i++) {
      this.callAddMedia(pBJNO, pPRTF, tabEMAL.get(i))
    }
  }

  /**
   * Call MNS215MI_AddMedia
   */
  public void callAddMedia(String pBJNO, String pPRTF, String email) {
    String subject = ""
    switch (pPRTF.trim()) {
      case "OIS606PF":
        subject = "Confirmation de commande"
        break
      case "QPS601PF":
        subject = "Devis"
        break
      case "OIS199PF":
        subject = "Facture de vente"
        break
      case "PPS601PF":
        subject = "Ordre Achat"
        break
      case "PPS821PF":
        subject = "Retour Fournisseur"
        break
      case "MMS480PF":
        subject = "Bon Livraison"
        break
      case "MWS620PF":
        subject = "Avis d'Expédition"
        break
      default:
        subject = "Autre Edition"
        break
    }

    def params = ["BJNO": pBJNO.toString(), "PRTF": pPRTF.toString(), "MEDC": "*MAIL",
                  "TOMA": email, "FRMA": "M3@tanguy.fr", "SUBJ": subject]

    def callback = {}
    miCaller.call("MNS215MI", "AddMedia", params, callback)
  }
}
