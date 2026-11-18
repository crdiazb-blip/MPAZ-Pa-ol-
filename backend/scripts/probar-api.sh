#!/bin/bash
# Prueba de punta a punta del backend contra el api-gateway. Uso: bash e2e.sh [url-gateway]
G="${1:-http://localhost:8080}"
campo() { python3 -c "import sys,json; d=json.load(sys.stdin); print(eval('d'+sys.argv[1]))" "$1"; }
codigo() { curl -s -o /dev/null -w "%{http_code}" "$@"; }
json() { curl -s -H 'Content-Type: application/json' "$@"; }

echo "1) sin token                         -> $(codigo $G/api/equipos) (esperado 401)"
echo "2) login con clave mala              -> $(codigo -X POST $G/api/auth/login -H 'Content-Type: application/json' -d '{"rut":"12.345.678-5","password":"mala"}') (esperado 401)"
ADM=$(json -X POST $G/api/auth/login -d '{"rut":"11.111.111-1","password":"admin123"}')
TA=$(echo "$ADM" | campo "['token']")
echo "3) login admin                       -> rol $(echo "$ADM" | campo "['usuario']['rol']")"
DOC=$(json -X POST $G/api/auth/login -d '{"rut":"12.345.678-5","password":"1234"}')
TD=$(echo "$DOC" | campo "['token']")
echo "4) login docente                     -> rol $(echo "$DOC" | campo "['usuario']['rol']")"
echo "5) catalogo como docente             -> $(curl -s $G/api/equipos -H "Authorization: Bearer $TD" | python3 -c "import sys,json;d=json.load(sys.stdin);print(len(d),'equipos')")"
echo "6) docente crea equipo               -> $(codigo -X POST $G/api/equipos -H "Authorization: Bearer $TD" -H 'Content-Type: application/json' -d '{"nombre":"x","marca":"x","modelo":"x","categoria":"OTRA","stockTotal":1,"ubicacion":"x"}') (esperado 403)"
echo "7) /api/internal no expuesto         -> $(codigo -X POST $G/api/internal/equipos/reservar -H "Authorization: Bearer $TD") (esperado 404)"

NUEVO=$(json -X POST $G/api/equipos -H "Authorization: Bearer $TA" -d '{"nombre":"Proyector de prueba","marca":"Epson","modelo":"E1","categoria":"PROYECTOR","stockTotal":3,"ubicacion":"Panol - Estante C"}')
EID=$(echo "$NUEVO" | campo "['id']")
echo "8) admin crea equipo                 -> id $EID, stock $(echo "$NUEVO" | campo "['stockDisponible']")/$(echo "$NUEVO" | campo "['stockTotal']")"

MANANA=$(python3 -c "import datetime;print((datetime.date.today()+datetime.timedelta(days=2)).isoformat())")
HOY=$(python3 -c "import datetime;print(datetime.date.today().isoformat())")
ORA=$(python3 -c "import datetime;print((datetime.datetime.now()+datetime.timedelta(minutes=30)).strftime('%H:%M'))")
echo "9) solicitud con <3h de anticipacion -> $(json -X POST $G/api/solicitudes -H "Authorization: Bearer $TD" -d "{\"sala\":\"Sala 1\",\"fecha\":\"$HOY\",\"horaInicio\":\"$ORA\",\"horaDevolucion\":\"23:59\",\"items\":[{\"equipoId\":$EID,\"cantidad\":1}]}" | campo "['message']")"
echo "10) solicitud pidiendo mas stock     -> $(json -X POST $G/api/solicitudes -H "Authorization: Bearer $TD" -d "{\"sala\":\"Sala 1\",\"fecha\":\"$MANANA\",\"horaInicio\":\"09:00\",\"horaDevolucion\":\"10:00\",\"items\":[{\"equipoId\":$EID,\"cantidad\":9}]}" | campo "['message']")"
SOL=$(json -X POST $G/api/solicitudes -H "Authorization: Bearer $TD" -d "{\"sala\":\"Sala 1\",\"fecha\":\"$MANANA\",\"horaInicio\":\"09:00\",\"horaDevolucion\":\"10:00\",\"items\":[{\"equipoId\":$EID,\"cantidad\":2}]}")
SID=$(echo "$SOL" | campo "['id']")
echo "11) solicitud valida                 -> id $SID estado $(echo "$SOL" | campo "['estado']")"
echo "12) docente intenta aceptar          -> $(codigo -X PATCH $G/api/solicitudes/$SID/aceptar -H "Authorization: Bearer $TD") (esperado 403)"
echo "13) admin rechaza sin justificacion  -> $(codigo -X PATCH $G/api/solicitudes/$SID/rechazar -H "Authorization: Bearer $TA" -H 'Content-Type: application/json' -d '{"comentario":""}') (esperado 400)"
echo "14) admin acepta                     -> $(json -X PATCH $G/api/solicitudes/$SID/aceptar -H "Authorization: Bearer $TA" | campo "['estado']")"
echo "15) stock tras aceptar               -> $(curl -s $G/api/equipos -H "Authorization: Bearer $TD" | python3 -c "import sys,json;d=[e for e in json.load(sys.stdin) if e['id']==$EID][0];print(d['stockDisponible'],'de',d['stockTotal'])") (esperado 1 de 3)"
echo "16) admin entrega                    -> $(json -X PATCH $G/api/solicitudes/$SID/entregar -H "Authorization: Bearer $TA" | campo "['estado']")"
echo "17) admin registra devolucion        -> $(json -X PATCH $G/api/solicitudes/$SID/devolver -H "Authorization: Bearer $TA" | campo "['estado']")"
echo "18) stock tras devolver              -> $(curl -s $G/api/equipos -H "Authorization: Bearer $TD" | python3 -c "import sys,json;d=[e for e in json.load(sys.stdin) if e['id']==$EID][0];print(d['stockDisponible'],'de',d['stockTotal'])") (esperado 3 de 3)"
echo "19) admin finaliza                   -> $(json -X PATCH $G/api/solicitudes/$SID/finalizar -H "Authorization: Bearer $TA" | campo "['estado']")"
echo "20) bitacora de la solicitud         -> $(curl -s "$G/api/bitacora?solicitudId=$SID" -H "Authorization: Bearer $TA" | python3 -c "import sys,json;print(' > '.join(r['hito'] for r in json.load(sys.stdin)))")"
echo "21) notificaciones del docente       -> $(curl -s $G/api/notificaciones -H "Authorization: Bearer $TD" | python3 -c "import sys,json;print(len(json.load(sys.stdin)),'recibidas')")"
echo "22) docente ve solo lo suyo          -> $(curl -s $G/api/solicitudes -H "Authorization: Bearer $TD" | python3 -c "import sys,json;print(len(json.load(sys.stdin)),'solicitud(es)')")"
echo "23) admin da de baja el equipo       -> $(codigo -X DELETE $G/api/equipos/$EID -H "Authorization: Bearer $TA") (esperado 204)"
echo "24) logout docente                   -> $(codigo -X POST $G/api/auth/logout -H "Authorization: Bearer $TD") (esperado 204)"
echo "25) token del docente tras logout    -> $(codigo $G/api/equipos -H "Authorization: Bearer $TD") (esperado 401)"
