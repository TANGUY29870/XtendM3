/**
 * README
 *
 * Name: EXT004MI.MajOrdrePassage
 * Description: Met à jour l'ordre de passage des livraisons
 * Date                         Changed By                         Description
 * 20240410                     ddecosterd@hetic3.fr     	création
 */
public class MajOrdrePassage extends ExtendM3Transaction {
	private final MIAPI mi
	private final ProgramAPI program
	private final DatabaseAPI database
	private final UtilityAPI utility

	public MajOrdrePassage(MIAPI mi, ProgramAPI program, DatabaseAPI database, UtilityAPI utility) {
		this.mi = mi
		this.program = program
		this.database = database
		this.utility = utility
	}

	public void main() {
		Integer CONO = mi.in.get("CONO")
		Long DLIX = mi.in.get('DLIX')
		Integer INOU = mi.in.get('INOU')
		Integer SULS = mi.in.get('SULS')
		Integer MULS = mi.in.get('MULS')

		if(!checkInputs(CONO, DLIX, INOU))
			return

		DBAction mhdishRecord = database.table("MHDISH").index("00").build()
		DBContainer mhdishContainer = mhdishRecord.createContainer()
		mhdishContainer.setInt("OQCONO", CONO)
		mhdishContainer.setLong("OQDLIX", DLIX)
		mhdishContainer.setInt("OQINOU", INOU)

		if(!mhdishRecord.readLock(mhdishContainer,{ LockedResult updatedRecord ->
					if(SULS != null) {
						updatedRecord.setInt("OQSULS", SULS)
					}
					if(MULS != null) {
						updatedRecord.setInt("OQMULS", MULS)
					}
					if(SULS != null || MULS != null) {
						int CHNO = updatedRecord.getInt("OQCHNO")
						if(CHNO== 999) {
							CHNO = 0
						}
						updatedRecord.set("OQLMDT", (Integer) this.utility.call("DateUtil", "currentDateY8AsInt"))
						updatedRecord.set("OQCHID", this.program.getUser())
						updatedRecord.setInt("OQCHNO", CHNO)
						updatedRecord.update()
					}
				})) {
			mi.error("Enregistrement non existant.")
			return
		}
	}
	
	/**
	 * Check input params
	 * @param cono
	 * @param dlix
	 * @param inou
	 * @return
	 */
	private boolean checkInputs(Integer cono, Long dlix, Integer inou) {
		if(cono == null) {
			mi.error("La division est obligatoire.")
			return false
		}

		DBAction query = database.table("CMNCMP").index("00").build()
		DBContainer container = query.getContainer()
		container.set("JICONO", cono)
		if(!query.read(container)) {
			mi.error("La division est inexistante.")
			return false
		}

		if(dlix == null) {
			mi.error("L'identifiant de livraison est obligatoire.")
			return false
		}

		if(inou == null) {
			mi.error("La direction de livraison est obligatoire.")
			return false
		}

		return true
	}
}
