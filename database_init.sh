mongosh admin -u "${MONGO_INITDB_ROOT_USERNAME}" \
          -p "${MONGO_INITDB_ROOT_PASSWORD}" \
          --eval "db.createUser({user: '${MONGO_USER}', pwd: '${MONGO_PASSWORD}', roles:[{role:'readWrite', db: '${MONGO_DATABASE}'}]});"
