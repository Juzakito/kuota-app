// Carga los documentos config/* en Firestore con Admin SDK.
// Uso:
//   GOOGLE_APPLICATION_CREDENTIALS=./sa-<entorno>.json npx ts-node seed-config.ts --project kuota-dev
// Nunca ejecutar con credenciales de prod desde un equipo personal sin revisión: usar Cloud Shell o CI.

import { initializeApp, cert, applicationDefault } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import seed from "./firestore_config_seed.json";

const projectArg = process.argv.indexOf("--project");
const projectId = projectArg > -1 ? process.argv[projectArg + 1] : undefined;
if (!projectId) throw new Error("Falta --project <id>");

initializeApp({ credential: applicationDefault(), projectId });
const db = getFirestore();

async function main() {
  const entries = Object.entries(seed.config);
  const batch = db.batch();
  for (const [docId, data] of entries) {
    const ref = db.collection("config").doc(docId);
    batch.set(ref, { ...(data as object), updatedAt: new Date(), updatedBy: "seed-script" }, { merge: true });
  }
  await batch.commit();
  console.log(`config/* cargado en ${projectId}: ${entries.map(([k]) => k).join(", ")}`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
