# Cómo publicar Spectralssic (guía rápida)

## 1. Crear tu llave de firma (una sola vez)
Desde una terminal (necesitas Java instalado):

    keytool -genkeypair -v -keystore keystore.jks -alias deco -keyalg RSA -keysize 2048 -validity 10000

Guarda `keystore.jks` y sus contraseñas en un lugar seguro. **No lo subas a GitHub** (ya está en `.gitignore`).
Si lo pierdes, no podrás actualizar la app para quien ya la instaló.

## 2. Guardar los secretos en GitHub
En tu repositorio: **Settings → Secrets and variables → Actions → New repository secret**:

| Nombre            | Valor                                                    |
| ----------------- | -------------------------------------------------------- |
| `KEYSTORE_BASE64` | el archivo codificado: `base64 -w0 keystore.jks`          |
| `KEY_ALIAS`       | `deco` (el alias del paso 1)                              |
| `KEY_PASSWORD`    | la contraseña de la llave                                 |
| `STORE_PASSWORD`  | la contraseña del almacén                                 |

En Windows (PowerShell): `[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks"))`

## 3. Publicar una versión
1. (Opcional) sube `versionCode` y `versionName` en `app/build.gradle.kts`.
2. Crea y sube una etiqueta:

       git tag v1.0.0
       git push origin v1.0.0

3. GitHub Actions compila el APK firmado y lo publica en **Releases**. Tarda unos 10-20 minutos.
4. La gente entra a *Releases* y descarga `Spectralssic-v1.0.0.apk`.

## 4. Firebase (cuentas y amigos)
- En Firebase → **Authentication**: activa *Correo electrónico/contraseña*.
- En **Firestore → Reglas**: pega el contenido de `firestore.rules` y publica.
- Para mayor seguridad, en Google Cloud Console → *APIs y servicios → Credenciales*, restringe tu
  clave de API a tu app Android (paquete `com.otavolrosinante.spectralssic` y el SHA-1 de tu llave de firma).
- Si quieres iniciar sesión con Google, agrega el SHA-1 de tu llave en Firebase → Configuración del proyecto.

## 5. Antes de publicar, revisa
- [ ] README: pon tu correo de contacto y tu usuario de GitHub.
- [ ] Ícono y nombre de la app (el nombre ya es "Spectralssic").
- [ ] Que no haya contraseñas ni archivos `.jks` en el repositorio.
- [ ] Mantén el archivo `LICENSE` (GPL-3.0) y los créditos de *Special Thanks*.
