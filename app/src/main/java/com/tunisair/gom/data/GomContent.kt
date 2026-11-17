package com.tunisair.gom.data

import com.tunisair.gom.data.model.Chapter
import com.tunisair.gom.data.model.Section

/**
 * Chapter content extracted from the Tunisair Ground Operations Manual, Revision 09,
 * effective date 01/10/16. English is the only official version (GOM 0.4).
 *
 * The text below is a condensed rendering of the manual's procedures for on-device
 * reference; the official manual on the Tunisair intranet remains the controlling
 * document.
 */
object GomContent {

    const val MANUAL_REVISION = "09"
    const val EFFECT_DATE = "01/10/16"
    const val POSTHOLDER = "Montassar Bnouni"
    const val QUALITY_MANAGER = "Sami Attar"

    /** Contact published in GOM 0.5.1 for amendment receipt acknowledgement. */
    const val NORMAS_EMAIL = "normes.procedures@tunisair.com.tn"

    val chapters: List<Chapter> = listOf(
        chapter0(), chapter1(), chapter2(), chapter3(), chapter4(),
        chapter5(), chapter6(), chapter7(), chapter8(), chapter9(),
        chapter10(), chapter11(), chapter12()
    )

    fun chapterById(id: Int): Chapter? = chapters.firstOrNull { it.id == id }

    fun search(query: String): List<Hit> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val hits = mutableListOf<Hit>()
        chapters.forEach { ch ->
            if (ch.title.lowercase().contains(q) || ch.titleArabic.contains(query.trim())) {
                hits += Hit(ch.id, ch.number, ch.title, "Chapter", ch.title)
            }
            ch.sections.forEach { sec ->
                val secTitle = "${sec.id} ${sec.title}"
                if (secTitle.lowercase().contains(q)) {
                    hits += Hit(ch.id, ch.number, ch.title, sec.id, sec.title)
                }
                if (sec.content.lowercase().contains(q)) {
                    val snippet = snippet(sec.content, q)
                    if (snippet != null) {
                        hits += Hit(ch.id, ch.number, ch.title, sec.id, sec.title, snippet)
                    }
                }
                sec.subsections.forEach { sub ->
                    if ("${sub.id} ${sub.title}".lowercase().contains(q) ||
                        sub.content.lowercase().contains(q)
                    ) {
                        val snippet = snippet(sub.content, q)
                        hits += Hit(ch.id, ch.number, ch.title, sub.id, sub.title, snippet)
                    }
                }
            }
        }
        return hits
    }

    private fun snippet(text: String, q: String): String? {
        val idx = text.lowercase().indexOf(q)
        if (idx < 0) return null
        val start = (idx - 60).coerceAtLeast(0)
        val end = (idx + q.length + 120).coerceAtMost(text.length)
        val body = text.substring(start, end).replace('\n', ' ').trim()
        return (if (start > 0) "…" else "") + body + (if (end < text.length) "…" else "")
    }

    data class Hit(
        val chapterId: Int,
        val chapterNumber: String,
        val chapterTitle: String,
        val sectionId: String,
        val sectionTitle: String,
        val snippet: String? = null
    )

    private fun section(id: String, title: String, content: String) =
        Section(id = id, title = title, content = content.trim())

    // ---------------- CHAPTER 0 ----------------
    private fun chapter0() = Chapter(
        id = 0,
        number = "00",
        title = "Introduction",
        titleArabic = "مقدمة",
        color = 0xFF1565C0,
        sections = listOf(
            section(
                "0.1", "Introduction",
                """
                The Ground Operations Manual (GOM) is issued under the authority of the Post Holder
                Ground Operations.

                The procedures and instructions contained in the GOM are based on the latest technical
                data and operational experience.

                The GOM has been prepared for use by TUNISAIR staff, handlers and/or subcontractors.
                It is issued to each airport station manager and to all other persons and official
                bodies concerned with TUNISAIR ground operations.

                All employees shall be familiar with the laws, regulations, procedures and the contents
                of this manual relevant to the performance of their duties. Any misuse or deviation of
                Company Operational Safety Standards is treated as a violation. A deliberate violation
                may lead to appropriate consequences.

                The reporting of unpremeditated or inadvertent errors will not result in disciplinary
                or punitive action being taken against the reporter or other individuals (Voluntary
                Safety Report System) unless it can otherwise be proven that such errors were the
                result of illegal activity, gross negligence or wilful misconduct.

                Nothing in the GOM shall prevent personnel from exercising their own best judgment
                during any irregularity for which the GOM gives no provisions, or in emergencies.

                All operational staff performing safety related work shall be physically and mentally
                fit for duty.

                Should any individual consider that all or any part of a procedure or instruction
                requires to be amended, he must notify the Post Holder Ground Operations and/or his
                respective superior.
                """.trimIndent()
            ),
            section(
                "0.2", "List of Amendments",
                """
                Revision 00  01/08/05  All — new document.
                Revision 01  02/10/06  Distribution list, baggage segregation, e-ticket, check-in
                procedures, medical clearance, PRM, DGR acceptance summary, pushback operations,
                sealing of aircraft, boarding announcement, Tunisair forms.
                Revision 02  15/09/08  Work environment, qualifications and experience, carry-on
                baggage check/removal, load control, flight files, stretchers, seating instructions.
                Revision 03  01/05/09  Baggage irregularities, denied boarding compensation,
                piece concept, weights and volumes, HEA loading position, cabin versions.
                Revision 04  01/09/10  Quality system, check-in desk, denial of carriage, child and
                infant handling, UM/YP, delay codes, EDP loadsheet, leaking goods, cabin seats for
                cargo, evacuation of aircraft, undeclared dangerous goods, risk management,
                de/anti-icing.
                Revision 05  30/03/12  Distribution list, quality assurance, station audits,
                manuals, endorsement waiver, e-ticket interline, mobile check-in, PRM seating,
                return check-in, golf equipment, medical oxygen, baggage tracing, NOTOC.
                Revision 06  01/01/13  Organisation chart, parking, fuel and oil, delay table,
                pooling of baggage, dangerous goods summary, aircraft limitations, ground support
                equipment, fuel spillage, holdover time.
                Revision 07  01/10/14  Post holder, boarding sequence, medical clearance, UM seating,
                dry ice, batteries, load control responsibilities, LMC, NOTOC, loading restrictions,
                fuel/defuelling training, storage.
                Revision 08  01/06/15  Safety management system, cargo/mail handling, manuals,
                medical clearance, deportees and passengers in custody, boarding discrepancies,
                DCS breakdown, refusal of embarkation, lithium batteries, aircraft weights,
                ground damages, refuelling, holdover table.
                Revision 09  01/10/16  Amendments, distribution list, check-in positions, lounges,
                mobile and online check-in, UM/YP restrictions, Fidelis program, refusal of
                embarkation, piece concept, medical oxygen and POC, lithium batteries, LMC entry,
                air-conditioning, weight and volume limitations, usable and capacity fuel, ground
                damages, refuelling, holdover table, boarding announcements, forms.
                """.trimIndent()
            ),
            section(
                "0.3", "Purpose and Scope",
                """
                The main purpose of this manual is to ensure handling of Tunisair aircraft, passengers,
                baggage, cargo, mail and COMAT at all stations operated by Tunisair in accordance with
                the Civil Aviation Regulations of the Tunisian DGAC (Decision 242), EU-OPS and
                Tunisair's Air Operator Certificate (AOC).

                This manual is meant to be a general guideline and source of information for
                procedures, practices and company policies for all personnel engaged in aircraft and
                passenger handling duties.
                """.trimIndent()
            ),
            section(
                "0.4", "Use of Manual",
                """
                The English version of the manual is the only official version.

                The official version of the Ground Operations Manual can be found on the Tunisair
                intranet, or sent by e-mail or CD-ROM.

                Each page has a header which indicates the effective date, the chapter and page number.

                The Tunisair Ground Operations Manual must be available to all personnel engaged in
                Tunisair aircraft and passenger handling duties.
                """.trimIndent()
            ),
            section(
                "0.5", "Maintaining and Revisioning",
                """
                When the manual is amended, a new CD-ROM is sent to each holder of the manual in
                accordance with the distribution list. The manual on the Tunisair intranet is amended
                accordingly.

                The manual holder must acknowledge receipt of the communicated revision via mail or
                A/R receipt: normes.procedures@tunisair.com.tn

                The latest revised, new information is indicated with a different colour in the text.

                CD-ROMs which are no longer needed shall be returned to the Tunisair Normes et
                Procedures Department, which shall also be informed of any change of GOM holder or
                change in the holder's address.
                """.trimIndent()
            ),
            section(
                "0.6", "Distribution List",
                """
                ABJ Representative 2 — ABJ
                ALG Station Manager 2
                BCN Representative 2
                BEG Representative 2
                BEN Station Manager 4 — TOB, LAQ
                BEY Representative 2
                BKO Representative 2
                BRU Station Manager 5 — LGG, AMS, OST
                CAI Representative 2
                CMN Station Manager 2
                DKR Representative 2
                DJE Area Manager 2
                DUS Representative 2
                FCO Station Manager 2
                FRA Area Manager 10 — SXF, BRE, CGN, HAM, HAJ, LEJ …
                GVA Station Manager 2
                IST Station Manager 2
                JED Station Manager 2
                LHR Station Manager 4 — LHR, LGW, MAN
                LIS Station Manager 2
                LYS Station Manager 2
                MAD Station Manager 3 — MAD, BIO
                MED Station Manager 2
                MIR Area Manager 2
                MRS Station Manager 2
                MUC Representative 2
                MOW Representative 4 — SVO, ROV, LED
                MXP Station Manager 2
                NBE Station Manager 2
                NCE Station Manager 2
                NIM Representative 2
                NKC Representative 2
                ORN Station Manager 2
                ORY Area Manager 6 — ORY, CDG, NTE, SXB, LIL
                OUA Representative 2
                SFA Station Manager 2
                TIP Station Manager 4 — TIP, MRA
                TLS Representative 3 — TLS, BOD
                TOE Station Manager 2
                TUN Area Manager 3 — TBJ
                VCE Station Manager 3 — VCE, BLQ
                VIE Representative 10 — VIE, LNZ, GRZ, BUD, KTW, KSC, KRK, WAW, PRG
                YUL Representative 2
                ZRH Station Manager 2
                """.trimIndent()
            ),
            section(
                "0.7", "Amendment Proposal Form",
                """
                Proposed Amendment: (continue on a separate sheet if necessary)
                Reason for Amendment:
                Employee Name / Position / Signature

                Guidelines on the completion of this form:
                1. Proposed amendment to a manual will only be considered if submitted on this form.
                2. This form should be completed, signed and handed to the GOM manager.
                3. Tunisair reserves the right to amend any proposal.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 1 ----------------
    private fun chapter1() = Chapter(
        id = 1,
        number = "01",
        title = "Organisation",
        titleArabic = "التنظيم",
        color = 0xFF00695C,
        sections = listOf(
            section(
                "1.2.2", "Nominated Postholder Ground Operations",
                """
                The Senior Manager Ground Services (Directeur Central Support du Produit) is the
                nominated postholder accepted by the Tunisian CAA for ground operations, and the
                Director of Ground Operations is the assistant postholder.

                The nominated postholder reports to the Accountable Manager and is required to:

                - Ensure the safe, punctual and economic operation of all company flights by
                  establishing or contracting professional ground handling of company aircraft,
                  their load and all passengers at all airports the company operates to.
                - Ensure requirements for the security and safety of aircraft, passengers, baggage,
                  cargo, COMAT and mail, in cooperation with the relevant authority responsible for
                  security at foreign airports.
                - Publish and keep up to date appropriate directives for ground handling personnel
                  and aircraft type specific procedures needed for mass and centre of gravity
                  calculation with respect of limitations, safety, security, handling of accidents
                  and occurrences, and initial and recurrent training and qualifications for service
                  provider personnel.
                - Ensure that legal requirements and the provisions of the AOC relating to ground
                  operations are strictly followed.
                - Act as company representative to all authorities as far as ground operations are
                  concerned.
                - Be responsible for contracts with companies providing ground handling services.
                - Supervise contracted companies in order to assure that required standards are met,
                  since the company retains the overall responsibility.
                """.trimIndent()
            ),
            section(
                "1.3.4", "Document Control",
                """
                Manuals and technical data used for ground operations shall be controlled so that only
                the current revision is available at the point of use.

                Superseded pages shall be removed from working copies as soon as a revision is
                received, and the holder shall acknowledge receipt in accordance with GOM 0.5.1.

                Records of acknowledgement, audit reports and training evidence shall be retained so
                that compliance can be demonstrated to the Tunisian CAA on request.
                """.trimIndent()
            ),
            section(
                "1.3.5", "Quality Assurance Programme",
                """
                The quality assurance programme covers quality inspection and audits.

                Quality inspection is performed on live turnarounds and against the standards defined
                in this manual. Findings are recorded and tracked to closure.

                Audits follow a published schedule and cover the scope of ground handling contracted
                by the company at each station.

                Auditors shall be independent of the activity being audited.

                Non-conformance, preventive and corrective actions are raised for every finding, with
                an owner and a due date, and their effectiveness is verified at the next scheduled
                audit or inspection.
                """.trimIndent()
            ),
            section(
                "1.4", "Safety Management System",
                """
                The Safety Management System (SMS) is the formal, top-level business-like approach to
                managing safety risk, with a high level of safety assurance.

                The SMS is built on four components:

                1. Safety policy and objectives.
                2. Safety risk management.
                3. Safety assurance.
                4. Safety promotion.

                Safety risk management identifies hazards, assesses the associated risk and applies
                mitigation before the risk is tolerated.

                Safety review is performed by the Safety Review Board (SRB), supported by the Safety
                Action Group (SAG) and the SMS manager.

                Accidents and occurrences are handled according to the procedures below.
                """.trimIndent()
            ),
            section(
                "1.4.5", "Handling of Accidents and Occurrences",
                """
                Serious incident — an incident involving circumstances indicating that there was a
                high probability of an accident.

                Incident — an occurrence, other than an accident, associated with the operation of an
                aircraft which affects or could affect the safety of operation.

                Serious injury — any injury which is sustained by a person in an accident and which
                involves a stay in hospital of more than 48 hours, commencing within seven days from
                the date the injury was received, or which results in a fracture of any bone (except
                simple fractures of fingers, toes or nose), or lacerations of any tissue involving
                internal organs, or which involves second or third degree burns or any burns
                affecting more than 5 percent of the body surface, or which involves confirmed
                exposure to infectious substances or harmful radiation.

                Ground incidents and dangerous goods incidents and accidents shall be reported
                immediately to the Post Holder Ground Operations and to the competent authority.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 2 ----------------
    private fun chapter2() = Chapter(
        id = 2,
        number = "02",
        title = "Ground Handling Agreement",
        titleArabic = "اتفاقية الخدمات الأرضية",
        color = 0xFF2E7D32,
        sections = listOf(
            section(
                "2.2", "IATA Standard Ground Handling Agreement",
                """
                Ground handling at each station is governed by a written agreement based on the IATA
                Standard Ground Handling Agreement (SGHA), as amended by Tunisair.

                Annex A defines the scope of services; Annex B the rate basis; Annex C liability
                limitations.

                The agreement identifies which party is responsible for each function so that no
                ground task is left unassigned.

                Any deviation from this manual at a station requires the written agreement of the
                Post Holder Ground Operations.
                """.trimIndent()
            ),
            section(
                "2.3.1.4", "Qualifications and Experience",
                """
                Personnel performing safety related duties shall be trained and qualified for the tasks
                they perform.

                Training records shall be maintained by the handler and made available for audit.

                Recurrent training shall be performed at the intervals defined in this manual and
                whenever a procedure is significantly amended.
                """.trimIndent()
            ),
            section(
                "2.3.7", "Load Control",
                """
                The handling agent provides load control in accordance with the responsibilities defined
                in chapter 7, including preparation of the loadsheet, entry of last minute changes and
                notification of special loads to the commander.
                """.trimIndent()
            ),
            section(
                "2.4", "Cargo and Mail Handling Procedures",
                """
                Cargo and mail handling shall be performed in accordance with chapter 8 of this manual,
                including inspection for damage or leakage, securing of load and the special
                regulations applicable to restricted articles.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 3 ----------------
    private fun chapter3() = Chapter(
        id = 3,
        number = "03",
        title = "Duties and Responsibilities",
        titleArabic = "الواجبات والمسؤوليات",
        color = 0xFF558B2F,
        sections = listOf(
            section(
                "3.3.2", "Manuals",
                """
                Each station shall hold the current revision of this manual together with the
                applicable aircraft, security and dangerous goods documentation.

                Manuals shall be available at the point of use and shall be amended as soon as a
                revision is received.
                """.trimIndent()
            ),
            section(
                "3.3.3", "Flight Files",
                """
                A flight file is raised for each departure and contains the documentation required for
                the flight, including the loadsheet, NOTOC where applicable, and the passenger and
                baggage documentation required by the regulations of the states concerned.

                The flight file shall be complete before the aircraft is accepted for departure.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 4 ----------------
    private fun chapter4() = Chapter(
        id = 4,
        number = "04",
        title = "Ticketing and Reservation",
        titleArabic = "التذاكر والحجز",
        color = 0xFF00838F,
        sections = listOf(
            section(
                "4.1.4", "Companies List / Endorsement Waiver Agreement",
                """
                Interline and codeshare arrangements are operated under bilateral agreements with the
                participating carriers. The endorsement waiver agreement records which carriers waive
                the requirement for endorsement of the ticket at the point of check-in.
                """.trimIndent()
            ),
            section(
                "4.1.5", "Code Share Agreements",
                """
                Code share flights are operated under a written code share agreement with the
                operating carrier. The operating carrier's ground handling standards apply at the
                point of departure unless the agreement states otherwise.
                """.trimIndent()
            ),
            section(
                "4.3", "Tunisair E-Ticket",
                """
                Electronic tickets are issued and controlled in accordance with IATA Resolution 722f.

                Interline e-ticket validity shall be verified before boarding.

                The passenger coupon status shall be checked against the departure control system
                record at check-in.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 5 ----------------
    private fun chapter5() = Chapter(
        id = 5,
        number = "05",
        title = "Passenger and Baggage Handling",
        titleArabic = "التعامل مع الركاب والأمتعة",
        color = 0xFF4527A0,
        sections = listOf(
            section(
                "5.1.4", "Check-in Procedures",
                """
                Check-in positions shall be staffed in accordance with the station plan and opened no
                later than the time specified in the handling agreement.

                At check-in the agent shall verify the passenger document, the ticket or e-ticket
                status, the baggage allowance, and any special service request.

                Baggage shall be checked in accordance with GOM 5.1.4.4 and the applicable allowance.

                Manual check-in is used when the departure control system is unavailable, and the
                passenger and baggage details are then keyed after the system is restored.
                """.trimIndent()
            ),
            section(
                "5.1.4.4", "Baggage Check-in",
                """
                Each checked bag shall be weighed, labelled and accepted against the allowance
                applicable to the passenger's fare.

                Bags outside the allowance are handled under GOM 5.10.4 (excess baggage).

                Restricted and dangerous goods shall be identified at acceptance in accordance with
                GOM 5.10.6.
                """.trimIndent()
            ),
            section(
                "5.1.5", "The Mobile and Online Check-in",
                """
                Passengers holding a mobile or online boarding pass may proceed directly to the
                boarding gate with cabin baggage only, where the station and the departure airport
                accept mobile boarding passes.

                The boarding pass barcode shall be scanned and the coupon status verified.

                Passengers with hold baggage shall proceed to a bag drop position.
                """.trimIndent()
            ),
            section(
                "5.2.1", "Passengers with Reduced Mobility (PRM)",
                """
                PRM are accepted and handled in accordance with the applicable regulation (EU) 1107/2006.

                Assistance shall be provided from the point of arrival at the airport to the aircraft
                seat, and from the seat to the point of leaving the airport on arrival.

                PRM shall not be left unattended at any point during the assistance.
                """.trimIndent()
            ),
            section(
                "5.2.1.2.4", "Medical Clearance",
                """
                Medical clearance is required for passengers whose condition may adversely affect the
                safety or comfort of other passengers or crew, or who require in-flight medical
                attention or oxygen.

                The clearance shall be obtained from the company medical service before acceptance.

                Passengers requiring oxygen are handled under GOM 5.10.6.2.2 (medical oxygen and POC).
                """.trimIndent()
            ),
            section(
                "5.2.1.7", "Stretchers",
                """
                A passenger travelling on a stretcher requires medical clearance, a stretcher fitted to
                the aircraft in the position defined in chapter 9, and a seat reservation blocking the
                seats occupied by the stretcher.

                The stretcher and the passenger's medical equipment are carried as defined in chapter 9.
                """.trimIndent()
            ),
            section(
                "5.2.4", "Unaccompanied Minors (UM) and Young Persons (YP)",
                """
                A UM is a child travelling without a parent, guardian or a passenger of at least
                18 years authorised to accompany the child.

                The UM handling form shall be completed in duplicate and the child handed over against
                signature at each point of transfer.

                The number of UM carried on a flight is restricted in accordance with GOM 5.2.4.2.

                A UM shall be seated in the last rows of the cabin where practicable, and shall be
                escorted during boarding and disembarkation.
                """.trimIndent()
            ),
            section(
                "5.5.4", "Pooling of Baggage",
                """
                Baggage belonging to passengers on the same flight is pooled. No individual passenger
                identification is retained after check-in except where required for special handling
                or for tracing.
                """.trimIndent()
            ),
            section(
                "5.6.1", "Procedure and Control",
                """
                Boarding control compares the number of passengers boarded against the number of
                passengers accepted for the flight.

                Any boarding discrepancy shall be resolved before the aircraft doors are closed, in
                accordance with GOM 5.6.1.1.

                Where the departure control system fails, boarding is performed in accordance with
                GOM 5.6.1.2.
                """.trimIndent()
            ),
            section(
                "5.6.3", "Refusal of Embarkation",
                """
                Embarkation may be refused where the passenger's conduct, health, documentation or
                security status is incompatible with the safety of the flight.

                The refusal shall be recorded and the reason notified to the captain where it affects
                the load or the security of the aircraft.

                Passengers refused embarkation are handled under the applicable compensation rules.
                """.trimIndent()
            ),
            section(
                "5.10.2.1", "Piece Concept",
                """
                Baggage is accepted and carried under the piece concept: the allowance is expressed as
                a number of pieces, each within the maximum weight and dimensions defined by the
                company.

                A piece exceeding the maximum weight or dimensions is accepted only as special baggage
                under GOM 5.10.5.
                """.trimIndent()
            ),
            section(
                "5.10.3", "Labelling of Baggage",
                """
                Each accepted bag receives a baggage tag showing the flight, date, destination and a
                unique bag tag number.

                The tag stub is retained by the passenger. Special labels are used for priority,
                fragile, heavy and valet baggage in accordance with GOM 5.10.3.5.
                """.trimIndent()
            ),
            section(
                "5.10.6.2.1", "Dry Ice",
                """
                Dry ice is accepted as dangerous goods only with the approval of the operator and within
                the limits of the applicable dangerous goods regulations.

                The package shall permit the release of carbon dioxide gas and shall be marked with the
                net weight of dry ice.
                """.trimIndent()
            ),
            section(
                "5.10.6.2.2", "Medical Oxygen and POC",
                """
                Portable oxygen concentrators (POC) approved by the manufacturer for aviation use are
                accepted on board subject to medical clearance.

                The passenger shall carry sufficient batteries for the whole journey.

                Spare and loose batteries are carried in accordance with GOM 5.10.6.5.11.
                """.trimIndent()
            ),
            section(
                "5.10.6.5.11", "Conditions for Transporting Batteries and Lithium Batteries",
                """
                Lithium batteries are forbidden or restricted according to their watt-hour rating and
                whether they are installed in equipment.

                Spare lithium batteries shall be carried in the cabin, in carry-on baggage, with the
                terminals protected against short circuit.

                Damaged, defective or recalled batteries are forbidden for air transport.

                Equipment powered by lithium batteries shall be protected against accidental activation.
                """.trimIndent()
            ),
            section(
                "5.11", "Baggage Handling Irregularities",
                """
                Delayed, lost, damaged or missing baggage is handled under this section and recorded in
                the WorldTracer system.

                A property irregularity report (PIR) is raised at the station for every irregularity.

                Baggage found without an owner is forwarded to the tracing office of the station where
                the bag was last seen.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 6 ----------------
    private fun chapter6() = Chapter(
        id = 6,
        number = "06",
        title = "Messages",
        titleArabic = "الرسائل",
        color = 0xFF6A1B9A,
        sections = listOf(
            section(
                "6.6", "Delay Codes",
                """
                Delay codes follow the IATA standard delay coding (IATA 11x/12x series), where the
                first two digits identify the broad cause and the last two the specific reason.

                Delay codes shall be recorded for every delayed departure and reported in the
                punctuality message.

                Examples: 11 passenger and baggage, 12 check-in and gate, 13 boarding, 15 aircraft,
                16 loading and equipment, 17 mail and cargo, 21 aircraft fuelling, 31 aircraft
                rotation, 41–47 reactionary (connecting passengers, baggage, aircraft), 51–57 cargo
                and mail, 61–67 flight operations and crewing, 71–77 weather, 81–83 airport and
                national authorities, 91–96 technical.
                """.trimIndent()
            ),
            section(
                "6.15", "Distribution",
                """
                Operational messages are addressed to the stations and departments listed in the
                station directory, using the address indicators defined by IATA.

                The distribution of each message type is defined in the station communication table.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 7 ----------------
    private fun chapter7() = Chapter(
        id = 7,
        number = "07",
        title = "Load Control",
        titleArabic = "التحكم في الحمل",
        color = 0xFFAD1457,
        sections = listOf(
            section(
                "7.1", "Load Control",
                """
                Load control is responsible for the preparation of the load and trim documentation for
                each departure, within the limitations defined in chapter 9.

                Load control shall ensure that the aircraft is loaded and trimmed within the certified
                limits and that the loadsheet is signed before the aircraft doors are closed.
                """.trimIndent()
            ),
            section(
                "7.1.3", "Responsibilities",
                """
                The load controller is responsible for:

                - Preparing and signing the loadsheet.
                - Applying the correct last minute changes under GOM 7.8.
                - Issuing the load instruction report under GOM 7.6.
                - Notifying the captain of special loads under GOM 7.9.1.
                - Retaining the documentation in the flight file.

                The captain retains the authority to refuse a loadsheet or a loading instruction that
                does not comply with the limitations of chapter 9.
                """.trimIndent()
            ),
            section(
                "7.6", "Loading Instruction Report",
                """
                The loading instruction report (LIR) tells the loading team how the aircraft is to be
                loaded, hold by hold and position by position.

                The LIR is prepared by load control and given to the loading supervisor before loading
                commences. It is amended if the load plan changes.

                Loading is performed in the sequence stated in the LIR so that the aircraft remains
                within its loading limits throughout the operation.
                """.trimIndent()
            ),
            section(
                "7.8", "Last Minute Changes",
                """
                A last minute change (LMC) is an approved change to the load made after the loadsheet
                has been prepared but before it is signed.

                An LMC is permitted only when the revised index remains within the certified limits and
                the change is of a type approved in GOM 7.8.3.

                The LMC is entered on the loadsheet, identified by a reference number, and signed.
                """.trimIndent()
            ),
            section(
                "7.9.1", "Special Load — Notification to Captain",
                """
                The captain shall be notified of special loads carried in the holds, including dangerous
                goods, live animals, perishable goods, human remains, valuable cargo and mail.

                The notification is given by the NOTOC and, where required, by direct contact with the
                loading supervisor.
                """.trimIndent()
            ),
            section(
                "7.10", "Training",
                """
                Load control staff and loading supervisors shall be trained and certified for the
                aircraft types they handle, with recurrent training and assessment at the intervals
                defined by the company.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 8 ----------------
    private fun chapter8() = Chapter(
        id = 8,
        number = "08",
        title = "Aircraft Loading",
        titleArabic = "تحميل الطائرة",
        color = 0xFFBF360C,
        sections = listOf(
            section(
                "8.1.1.5", "Inspection for Damage or Leakage",
                """
                Every item of cargo, mail and baggage accepted for loading shall be inspected for
                damage, leakage, odour or staining before it is loaded.

                Items found damaged, leaking or emitting an odour shall be rejected, segregated and
                reported to the load controller.

                Leaking dangerous goods are handled under GOM 8.1.1.4 and GOM 10.1.6.4.
                """.trimIndent()
            ),
            section(
                "8.1.3.3", "Securing Load",
                """
                Load shall be secured in each hold so that it cannot shift in flight or in turbulence.

                Nets, straps and locks shall be applied in accordance with the aircraft type
                procedures.

                Loose load, unrestrained containers and partially loaded positions shall not be
                accepted for departure.
                """.trimIndent()
            ),
            section(
                "8.1.4.5.1", "Use of Seats in Cabin",
                """
                Seats in the cabin may be used for cargo only where the aircraft loading manual permits
                it, with the seats secured and the load restrained to the certified limits.

                Seats released for cargo packs shall be identified on the loadsheet.
                """.trimIndent()
            ),
            section(
                "8.2.1", "Ballast",
                """
                Ballast is loaded only where required to bring the aircraft within its centre of gravity
                limits, and in the amount and position defined by the aircraft loading manual.

                Ballast is recorded separately on the loadsheet.
                """.trimIndent()
            ),
            section(
                "8.2.23.2", "Maximum Height of Packages and Minimum Separation Distance",
                """
                The maximum height of packages in each hold and the minimum separation distance between
                the load and the aircraft structure are defined by the aircraft type.

                Loads exceeding the permitted height shall not be accepted.

                The load shall not obstruct access to the fire extinguisher or the hold controls.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 9 ----------------
    private fun chapter9() = Chapter(
        id = 9,
        number = "09",
        title = "Aircraft Description",
        titleArabic = "وصف الطائرة",
        color = 0xFF37474F,
        sections = listOf(
            section(
                "9.1", "A319 / A320 Family",
                """
                Chapter 9 describes the aircraft types operated by the company, including their
                dimensions, weights and indexes, cabin versions, limitations and loading positions.

                The tables in this chapter are the reference for load control: maximum gross weights,
                basic weights and indexes, standard taxi fuel, dry operating weights and index, and
                the limitations of each compartment.

                The loadable weight for each flight is calculated from the applicable table minus the
                traffic load, and the loadsheet shall state the figures used.
                """.trimIndent()
            ),
            section(
                "9.1.6.5", "Stretcher",
                """
                A stretcher is carried only in the positions defined for the aircraft type, with the
                seats beneath blocked and the stretcher secured to the aircraft structure.

                The accompanying passenger is seated adjacent to the stretcher where the cabin layout
                permits.
                """.trimIndent()
            ),
            section(
                "9.2.8.1", "Aircraft Limitations",
                """
                The following limitations apply to loading and shall be respected for every departure:

                - Maximum take-off, landing, zero fuel and taxi weights.
                - Maximum and minimum centre of gravity for each configuration.
                - Compartment maximum weights, volumes and floor loading.
                - Maximum height of packages and minimum separation distance from the structure.
                - Prohibition on carrying certain goods in specific holds.

                Where the calculated figure exceeds any of these limitations, the load shall be
                adjusted before the loadsheet is signed.
                """.trimIndent()
            ),
            section(
                "9.5.16", "Weight and Volume Limitations",
                """
                The maximum weight and volume of each compartment are stated in the aircraft loading
                tables. Volume is often the limiting factor before weight for low density cargo.

                The floor loading limitation shall be checked for heavy concentrated items.
                """.trimIndent()
            ),
            section(
                "9.5.21", "Fuel",
                """
                Usable fuel is the fuel available for the flight. Capacity fuel is the total volume of
                the tanks.

                Fuel figures used on the loadsheet are taken from the flight documentation and shall
                match the fuel slip issued by the fuelling agent.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 10 ----------------
    private fun chapter10() = Chapter(
        id = 10,
        number = "10",
        title = "Ramp Handling",
        titleArabic = "المناولة على المدرج",
        color = 0xFFE65100,
        sections = listOf(
            section(
                "10.1.1", "Safety on the Ramp",
                """
                Only authorised personnel wearing high visibility clothing may enter the ramp area.

                Personnel shall remain clear of the aircraft propellers, jet blast and engine intakes
                while engines are running.

                Ground support equipment shall be parked clear of the aircraft and shall never be left
                unattended against the aircraft.
                """.trimIndent()
            ),
            section(
                "10.1.3", "Driving on the Ramp",
                """
                Vehicles on the ramp shall be driven at walking pace near aircraft, shall observe the
                designated routes and stop lines, and shall stop when a wing walker or marshaller
                signals.

                Drivers shall hold a valid airside driving permit for the category of vehicle.

                Only the minimum number of vehicles required for the turnaround shall be present
                around the aircraft.
                """.trimIndent()
            ),
            section(
                "10.1.6", "Aircraft Ground Damage",
                """
                Any damage caused to the aircraft, however minor, shall be reported immediately to the
                flight crew and to the station manager, and recorded in accordance with GOM 10.1.11.

                Ground damages are recorded with photographs, a statement of circumstances and the
                names of the personnel involved.
                """.trimIndent()
            ),
            section(
                "10.1.7.1", "Safety Cones — Placement and Removal",
                """
                Safety cones are placed at the positions defined by the aircraft type before any ground
                operation begins, and removed immediately before the aircraft is moved.

                Cones are never left in place during pushback or taxi.
                """.trimIndent()
            ),
            section(
                "10.1.11", "Ground Damages",
                """
                Ground damage reports shall be completed for every occurrence, including damage caused
                by the company's own equipment.

                The report is forwarded to the Post Holder Ground Operations and to the insurance and
                quality departments.

                Repeated damage at a station is treated as a safety issue and is reviewed by the SAG.
                """.trimIndent()
            ),
            section(
                "10.1.13", "Fuel and Oil",
                """
                Fuelling and defuelling are performed by trained personnel in accordance with GOM
                10.1.13.3.

                Fuelling shall not take place while passengers are boarding or disembarking unless
                the required safety conditions of the aircraft fuelling procedures are met.

                A fuel spillage is handled under GOM 10.1.13.1.2 and shall be reported.
                """.trimIndent()
            ),
            section(
                "10.2.2", "Departure Check",
                """
                Before the aircraft is moved, the pushback crew completes the departure check:
                doors closed, ground equipment clear, cones removed, chocks removed, headset connected
                and the area around the aircraft clear of personnel.

                The departure check is confirmed to the flight crew before pushback clearance is
                requested.
                """.trimIndent()
            ),
            section(
                "10.4.10", "De/Anti-icing and Holdover Time",
                """
                De/anti-icing is performed only by trained and certified personnel, using fluid of the
                correct type and concentration, applied according to the aircraft and fluid
                manufacturer procedures.

                The estimated holdover time is taken from the holdover time table for the fluid type,
                concentration, precipitation and temperature.

                The holdover table gives the range of holdover times; the lower figure is used for
                planning so that the aircraft departs within the certified window.

                If the holdover time is exceeded, the aircraft must be de-iced again before take-off.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 11 ----------------
    private fun chapter11() = Chapter(
        id = 11,
        number = "11",
        title = "Boarding Announcement Guidelines",
        titleArabic = "إرشادات إعلان الصعود",
        color = 0xFF00695C,
        sections = listOf(
            section(
                "11", "Boarding Announcement Guidelines",
                """
                Boarding announcements are made in a clear, calm and informative manner, in the
                languages required at the station.

                Pre-boarding is announced first, followed by boarding of passengers requiring
                assistance and families with young children, then general boarding by zone.

                Passengers shall be informed of the boarding gate, the boarding time, the boarding
                sequence and the baggage rules for the cabin.

                Boarding shall not be announced before the aircraft and the gate are ready to accept
                passengers.
                """.trimIndent()
            )
        )
    )

    // ---------------- CHAPTER 12 ----------------
    private fun chapter12() = Chapter(
        id = 12,
        number = "12",
        title = "Tunisair Forms",
        titleArabic = "نماذج الخطوط التونسية",
        color = 0xFF455A64,
        sections = listOf(
            section(
                "12", "Tunisair Forms",
                """
                The forms below are used in ground operations and are reproduced from this chapter.

                - Delay certificate: issued to a passenger on request when a delay affects a
                  connection or a subsequent journey.
                - Property irregularity report (PIR): raised for delayed, lost or damaged baggage.
                - NOTOC: notification to captain for special loads.
                - Loading instruction report (LIR): loading instructions for the loading team.
                - Unaccompanied minor handling form.
                - Amendment proposal form (GOM 0.7).
                - Ground damage report.

                Forms shall be completed in ink, signed, and retained with the flight file where they
                relate to a departure.
                """.trimIndent()
            )
        )
    )
}
