"""Interaction-wording sweep: reader text must not describe interactions the app does not have.
The app is touch-only. What it supports: a Reveal block ("Tap to reveal the answer") and tap-to-enlarge on figures. Steps, callouts, accordion panels,
hover and "buttons" are static, so wording about them is rewritten (English and Tamil together). Where the next block IS a Reveal block, English keeps the
instruction as "Tap" and the Tamil (already touch wording) is unchanged.
Each entry: old English string -> (new English, new Tamil).  TAP entries: old English -> new English only (Tamil kept)."""
TAP = {
    "**Click-to-Reveal Key Terms**": "**Tap-to-Reveal Key Terms**",
    "**Click-to-Reveal: Key Terms**": "**Tap-to-Reveal: Key Terms**",
    "**Click-to-Reveal Key Concepts**": "**Tap-to-Reveal Key Concepts**",
    "**Click-to-Reveal Key Definitions**": "**Tap-to-Reveal Key Definitions**",
    "**Click to Reveal — Core Definitions**": "**Tap to Reveal — Core Definitions**",
    "Click any card to reveal the key function of each enzyme or term.": "Tap any card to reveal the key function of each enzyme or term.",
    "Interactive Learning: Click to Reveal": "Interactive Learning: Tap to Reveal",
}
STEP_EN, STEP_TA = "Each step below gives its details.", "ஒவ்வொரு படியும் அதன் விவரங்களைக் கீழே தருகிறது."
QH_EN, QH_TA = "Answer hints follow each question.", "ஒவ்வொரு வினாவுக்கும் விடைக் குறிப்புகள் தரப்பட்டுள்ளன."
MA_EN, MA_TA = "A model answer follows each question.", "ஒவ்வொரு வினாவுக்கும் மாதிரி விடை தரப்பட்டுள்ளது."
ACT_EN, ACT_TA = "Key points for active recall practice follow under each topic.", "செயல்மிகு நினைவுகூரல் பயிற்சிக்கான முக்கியக் குறிப்புகள் ஒவ்வொரு தலைப்பின் கீழும் தரப்பட்டுள்ளன."
KEY_EN, KEY_TA = "Key term", "முக்கியச் சொல்"
STATIC = {
 "All diagrams are drawn with inline SVG — scalable, offline, and academically labeled. Hover over parts to highlight them.":
  ("All diagrams are drawn with inline SVG — scalable, offline, and academically labeled. Tap a diagram to enlarge it.",
   "அனைத்துப் படங்களும் இன்லைன் SVG-யால் வரையப்பட்டுள்ளன — அளவு மாற்றத்தக்கவை, இணையமின்றிப் பயன்படுத்தத்தக்கவை, கல்வித் தரத்தில் பெயரிடப்பட்டவை. படத்தைத் தொட்டுப் பெரிதாக்கலாம்."),
 "Click each step to reveal what happens at that stage.": ("Each step below shows what happens at that stage.", "ஒவ்வொரு படியும் அந்நிலையில் நடப்பதைக் கீழே காட்டுகிறது."),
 "Click each question to reveal answer hints and key points.": ("Each question is followed by answer hints and key points.", "ஒவ்வொரு வினாவுக்கும் விடைக் குறிப்புகளும் முக்கியக் கருத்துகளும் தரப்பட்டுள்ளன."),
 "Fig. 2. Milestones. Click-free summary: 1973 Cohen and Boyer made the first recombinant DNA; 1975 Köhler and Milstein made hybridomas; 1977 Sanger sequencing.":
  ("Fig. 2. Milestones. Summary: 1973 Cohen and Boyer made the first recombinant DNA; 1975 Köhler and Milstein made hybridomas; 1977 Sanger sequencing.", None),
 "Click a question to reveal the model answer.": (MA_EN, MA_TA),
 "Explore concepts through interactive accordions, click-to-reveal definitions, and highlight boxes. Engage actively with the content!":
  ("Explore the key concepts, definitions and highlight boxes below. Engage actively with the content!", "கீழுள்ள முக்கியக் கருத்துகள், வரையறைகள், முனைப்புப் பெட்டிகளை ஆராய்க. உள்ளடக்கத்துடன் ஆர்வமாக ஈடுபடுக!"),
 "Click each panel to expand / collapse": ("Each panel is shown in full.", "ஒவ்வொரு பலகமும் முழுமையாகக் காட்டப்படுகிறது."),
 "**Click-to-Reveal — Important Definitions**": ("**Important Definitions**", "**முக்கிய வரையறைகள்**"),
 "👆 Click each card to reveal its definition": ("Each card gives its definition.", "ஒவ்வொரு அட்டையும் அதன் வரையறையைத் தருகிறது."),
 "Click to reveal": (KEY_EN, KEY_TA),
 "**Click to reveal**": ("**" + KEY_EN + "**", "**" + KEY_TA + "**"),
 "A visual overview of how biotechnology branches connect to tools, products, and societal impacts — hover over each node to highlight it.":
  ("A visual overview of how biotechnology branches connect to tools, products, and societal impacts.", "உயிரித் தொழில்நுட்பவியலின் கிளைகள் கருவிகள், பொருட்கள், சமூகத் தாக்கங்களுடன் எவ்வாறு இணைகின்றன என்பதன் காட்சி மேலோட்டம்."),
 "Click each question to reveal the model answer": ("A model answer follows each question", "ஒவ்வொரு வினாவுக்கும் மாதிரி விடை தரப்பட்டுள்ளது"),
 "Click each step to reveal process details.": ("Each step below gives process details.", "ஒவ்வொரு படியும் செயல்முறை விவரங்களைக் கீழே தருகிறது."),
 "Click the step buttons or use Prev/Next to walk through cDNA synthesis. Each step shows a labeled SVG diagram and explanation.":
  ("Walk through cDNA synthesis step by step. Each step shows a labeled SVG diagram and explanation.", "cDNA தொகுப்பைப் படிப்படியாகப் பின்தொடர்க. ஒவ்வொரு படியும் பெயரிடப்பட்ட SVG படத்தையும் விளக்கத்தையும் காட்டும்."),
 "Click each question to expand the answer hints.": (QH_EN, QH_TA),
 "Click on each enzyme to expand and learn about its function and role.": ("Each enzyme's function and role is described below.", "ஒவ்வொரு நொதியின் செயல்பாடும் பங்கும் கீழே விளக்கப்பட்டுள்ளன."),
 "Click on each step to learn more about what happens at that stage.": ("Each step below describes what happens at that stage.", "ஒவ்வொரு படியும் அந்நிலையில் நிகழ்வதைக் கீழே விளக்குகிறது."),
 "Click each question to reveal answer hints for your exam preparation.": ("Answer hints for exam preparation follow each question.", "தேர்வுத் தயாரிப்புக்கான விடைக் குறிப்புகள் ஒவ்வொரு வினாவையும் தொடர்ந்து தரப்பட்டுள்ளன."),
 "Click each step to reveal details.": (STEP_EN, STEP_TA),
 "Click a step to see what happens.": ("Each step below shows what happens.", "ஒவ்வொரு படியும் நிகழ்வதைக் கீழே காட்டுகிறது."),
 "**Steps in Southern blotting (click each step)**": ("**Steps in Southern blotting**", "**சதர்ன் பிளாட்டிங்கின் படிகள்**"),
 "**Steps (click each step)**": ("**Steps**", "**படிகள்**"),
 "Gel Electrophoresis Workflow — Click Each Step": ("Gel Electrophoresis Workflow", "இழும மின்னாற்பிரித்தல்ப் பணிப்பாய்வு"),
 "Click any step to expand details.": (STEP_EN, STEP_TA),
 "Click each question to expand answer hints.": (QH_EN, QH_TA),
 "Click the view buttons to see what happens in each scenario. Click individual colonies to learn more.":
  ("The scenarios below show what happens, colony by colony.", "கீழுள்ள சூழ்நிலைகள் குழுமம் வாரியாக நிகழ்வதைக் காட்டுகின்றன."),
 "Petri Dish — Blue-White Screening Result (hover over colonies)": ("Petri Dish — Blue-White Screening Result", "பெட்ரி தட்டு — நீலம்-வெள்ளை சலிக்கை முடிவு"),
 "Gene Cloning Pipeline — Click Each Step": ("Gene Cloning Pipeline", "மரபணு குளோனாக்கக் குழாய்"),
 "Click any step to expand its details.": (STEP_EN, STEP_TA),
 "Each diagram shows the mechanism of gene entry for that method. Click or hover to interact.":
  ("Each diagram shows the mechanism of gene entry for that method. Tap a diagram to enlarge it.", "ஒவ்வொரு வரைபடமும் அம்முறையில் மரபணு நுழையும் இயங்குமுறையைக் காட்டுகிறது. பெரிதாக்க வரைபடத்தைத் தொடுக."),
 "Gene Delivery — Concept Flow (Click Steps)": ("Gene Delivery — Concept Flow", "மரபணு செலுத்தல் — கருத்துப் பாய்வு"),
 "The diagram below shows the temperature profile of a single PCR cycle. Click each stage to see details.":
  ("The diagram below shows the temperature profile of a single PCR cycle. Each stage is described below.", "கீழ்க்காணும் வரைபடம் ஒரு PCR சுழற்சியின் வெப்பநிலை அமைப்பைக் காட்டுகிறது. ஒவ்வொரு நிலையும் கீழே விளக்கப்பட்டுள்ளது."),
 "Click each stage card to learn more:": ("Each stage in more detail:", "ஒவ்வொரு நிலையும் விரிவாக:"),
 "PCR Workflow — Click Each Step": ("PCR Workflow", "PCR பணிப்பாய்வு"),
 "Click to expand answer hints.": ("Answer hints:", "விடைக் குறிப்புகள்:"),
 "Sanger Sequencing Workflow — Click Each Step": ("Sanger Sequencing Workflow", "சேங்கர் வரிசைப்படுத்தல் பணிப்பாய்வு"),
 "Recombinant Screening Workflow — Click Each Step": ("Recombinant Screening Workflow", "மறுகூட்டிணைவுச் சலிக்கைப் பணிப்பாய்வு"),
 "**CRISPR Mechanism Diagram (Click steps for details)**": ("**CRISPR Mechanism Diagram**", "**CRISPR இயக்கமுறை வரைபடம்**"),
 "Expand each topic for key points and click-to-reveal concepts for active recall practice.": (ACT_EN, ACT_TA),
 "Expand topics for key points and click-to-reveal for active recall practice.": (ACT_EN, ACT_TA),
 "Expand each topic for key points and use click-to-reveal for active recall.": (ACT_EN, ACT_TA),
 "Expand each topic for key points and click-to-reveal for active recall practice.": (ACT_EN, ACT_TA),
 "Click each topic to expand key concepts. Use for quick review and exam preparation.":
  ("Key concepts for each topic follow. Use them for quick review and exam preparation.", "ஒவ்வொரு தலைப்பின் முக்கியக் கருத்துகளும் தரப்பட்டுள்ளன. விரைவுத் திருப்புதல், தேர்வுத் தயாரிப்புக்குப் பயன்படுத்துக."),
 "**RFLP — Concept Diagram (Click steps for details)**": ("**RFLP — Concept Diagram**", "**RFLP — கருத்து வரைபடம்**"),
 "**RAPD — Concept Diagram (Click steps for details)**": ("**RAPD — Concept Diagram**", "**RAPD — கருத்து வரைபடம்**"),
 "**DNA Fingerprinting — Concept Diagram (Click steps)**": ("**DNA Fingerprinting — Concept Diagram**", "**DNA கைரேகைப் பதிவு — கருத்து வரைபடம்**"),
 "Click on each step in the **Interactive Flowchart** (next chapter) to explore details. Here is a concise overview:":
  ("Each step is explored in detail in the **Flowchart** (next chapter). Here is a concise overview:", "ஒவ்வொரு படியும் **ஓட்ட வரைபடத்தில்** (அடுத்த பகுதி) விரிவாக ஆராயப்படுகிறது. இங்கு சுருக்கமான மேலோட்டம்:"),
 "Explore the complete step-by-step ethanol production process. Click any step to expand its detailed description below.":
  ("Explore the complete step-by-step ethanol production process. Each step has a detailed description below.", "எத்தனால் உற்பத்தியின் முழுப் படிப்படியான செயல்முறையை ஆராய்க. ஒவ்வொரு படிக்கும் விரிவான விளக்கம் கீழே தரப்பட்டுள்ளது."),
 "Enzymes act as biological catalysts that drive each step of fermentation. Click each accordion panel to explore key enzymes.":
  ("Enzymes act as biological catalysts that drive each step of fermentation. Key enzymes are described below.", "நொதிகள் நொதித்தலின் ஒவ்வொரு படியையும் இயக்கும் உயிரியல் வினையூக்கிகள். முக்கிய நொதிகள் கீழே விளக்கப்பட்டுள்ளன."),
 "👆 Click each step to see detailed information": ("Each step gives detailed information.", "ஒவ்வொரு படியும் விரிவான தகவலைத் தருகிறது."),
 "**GenBank Record Structure (Click sections)**": ("**GenBank Record Structure**", "**GenBank பதிவின் அமைப்பு**"),
 "**Database Network — Interconnection Diagram (Click nodes)**": ("**Database Network — Interconnection Diagram**", "**தரவுத்தள வலையமைப்பு — இடைத்தொடர்பு விளக்கப்படம்**"),
 "Click any box in the diagrams below for an explanation.": ("The diagrams below show how the parts connect.", "கீழுள்ள விளக்கப்படங்கள் பகுதிகள் எவ்வாறு இணைகின்றன என்பதைக் காட்டுகின்றன."),
 "**Data Flow: Biological Data → Database → Analysis (Click nodes)**": ("**Data Flow: Biological Data → Database → Analysis**", "**தரவுப் பாய்வு: உயிரியல் தரவு → தரவுத்தளம் → பகுப்பாய்வு**"),
 "**SWISS-PROT within the UniProt System (Click nodes)**": ("**SWISS-PROT within the UniProt System**", "**UniProt அமைப்பில் SWISS-PROT**"),
}
# Prefix rules: callout headings "Click to Reveal — X" (static callouts) lose the instruction; "Click to Reveal" alone becomes "Key point".
PREFIX = [("Click to Reveal — ", "", "தொட்டு வெளிப்படுத்துக — ", ""), ("Click to Reveal — ", "", "வெளிப்படுத்த தொடுக — ", "")]
LINE_ONLY = ("Click to Reveal", "Key point", ("தொட்டு வெளிப்படுத்துக", "வெளிப்படுத்த தொடுக"), "முக்கியக் குறிப்பு")
# Element-wise rules for strings of the form "<emoji>⏎**Term**⏎Click to reveal" and mixed ones: handled in apply().
TA_SUB = [("வெளிப்படுத்தத் தொடுக", KEY_TA)]  # last line of the four fermentation term cards
