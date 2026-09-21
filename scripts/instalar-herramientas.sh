#!/bin/sh
# Instala JDK 21 y Maven en el home del usuario, sin sudo y sin apt.
# Se hace asi porque el apt de esta maquina esta bloqueado por un paquete roto
# (chromium-chromedriver en estado iU) y el kata no necesita herramientas
# instaladas a nivel de sistema.
set -e

DESTINO="$HOME/.local/herramientas"
VERSION_MAVEN="3.9.9"
mkdir -p "$DESTINO"

if [ ! -d "$DESTINO/jdk-21" ]; then
  echo "Descargando Temurin JDK 21..."
  curl -fsSL -o /tmp/jdk21.tar.gz \
    "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse"
  mkdir -p "$DESTINO/jdk-21"
  tar -xzf /tmp/jdk21.tar.gz -C "$DESTINO/jdk-21" --strip-components=1
  rm -f /tmp/jdk21.tar.gz
  echo "JDK 21 instalado en $DESTINO/jdk-21"
else
  echo "JDK 21 ya estaba instalado"
fi

if [ ! -d "$DESTINO/maven" ]; then
  echo "Descargando Maven $VERSION_MAVEN..."
  curl -fsSL -o /tmp/maven.tar.gz \
    "https://archive.apache.org/dist/maven/maven-3/$VERSION_MAVEN/binaries/apache-maven-$VERSION_MAVEN-bin.tar.gz"
  mkdir -p "$DESTINO/maven"
  tar -xzf /tmp/maven.tar.gz -C "$DESTINO/maven" --strip-components=1
  rm -f /tmp/maven.tar.gz
  echo "Maven instalado en $DESTINO/maven"
else
  echo "Maven ya estaba instalado"
fi

cat > "$DESTINO/entorno.sh" <<GUION
export JAVA_HOME="$DESTINO/jdk-21"
export MAVEN_HOME="$DESTINO/maven"
export PATH="\$JAVA_HOME/bin:\$MAVEN_HOME/bin:\$PATH"
GUION

LINEA=". $DESTINO/entorno.sh"
if ! grep -qF "$LINEA" "$HOME/.bashrc" 2>/dev/null; then
  printf '\n# Herramientas del kata de evaluacion tecnica\n%s\n' "$LINEA" >> "$HOME/.bashrc"
  echo "Agregado al .bashrc"
fi

. "$DESTINO/entorno.sh"
echo ""
java -version
mvn -v | head -1
