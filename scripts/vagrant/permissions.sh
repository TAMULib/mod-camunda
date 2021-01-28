#!/bin/bash

cd /sync

curl -X POST -H "X-Okapi-Tenant: tern" -H "Content-Type: application/json" https://folio-okapiq22.library.tamu.edu/authn/login -d '{"username": "tern_admin", "password": "admin"}' -D login-headers.tmp
token_header=$(cat login-headers.tmp | grep x-okapi-token)

user_json=$(curl -v -H "X-Okapi-Tenant: tern" -H "$token_header" -H "Content-Type: application/json" "https://folio-okapiq22.library.tamu.edu/users?query=username=tern_admin")

id_regex='([0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12})'

echo "user: $user_json"
if [[ $user_json =~ $id_regex ]]
then
  user_id="${BASH_REMATCH[1]}"
  echo "user id: $user_id"
else
  echo "Could not get tern_admin id!"
fi

user_perms_json=$(curl -v -H "X-Okapi-Tenant: tern" -H "$token_header" -H "Content-Type: application/json" "https://folio-okapiq22.library.tamu.edu/perms/users?query=userId=$user_id")

echo "user permissions: $user_perms_json"
if [[ $user_perms_json =~ $id_regex ]]
then
  perm_id="${BASH_REMATCH[1]}"
  echo "perm id: $perm_id"
else
  echo "Could not get tern_admin permission id!"
fi

# update tern_admin permissions, add all permissions for mod-workflow and mod-camunda
echo '{
  "id": "'"$perm_id"'",
  "userId": "'"$user_id"'",
  "permissions": [
    "process.all",
    "process-definition.all",
    "decision-definition.all",
    "task.all",
    "message.all",
    "action.all",
    "trigger.all",
    "workflow.all",
    "perms.all",
    "okapi.proxy.pull.modules.post",
    "login.all",
    "okapi.all",
    "users.all",
    "configuration.all",
    "tags.all",
    "users-bl.all",
    "notify.all",
    "inventory-storage.all",
    "validation.all",
    "inventory.all",
    "login-saml.all",
    "user-import.all",
    "codex.all",
    "circulation-storage.all",
    "circulation.all",
    "calendar.collection.all",
    "notes.all",
    "finance.module.all",
    "orders.all",
    "templates.all",
    "rtac.all",
    "orders-storage.module.all",
    "module.myprofile.enabled",
    "ui-myprofile.view",
    "settings.tags.enabled",
    "settings.transfers.all",
    "module.orders.enabled",
    "module.organization.enabled",
    "settings.data-import.enabled",
    "module.eholdings.enabled",
    "ui-inventory.settings.instance-formats",
    "ui-checkin.all",
    "ui-circulation.settings.fixed-due-date-schedules",
    "ui-inventory.settings.instance-types",
    "ui-checkout.all",
    "ui-circulation.settings.cancellation-reasons",
    "settings.loan-rules.all",
    "module.developer.enabled",
    "module.finance.enabled",
    "ui-inventory.all-permissions.TEMPORARY",
    "settings.loan-policies.all",
    "module.data-import.enabled",
    "settings.developer.enabled",
    "settings.orders.enabled",
    "settings.eholdings.enabled",
    "ui-inventory.settings.materialtypes",
    "ui-inventory.settings.contributor-types",
    "ui-inventory.settings.loantypes",
    "ui-users.editperms",
    "ui-users.loans.renew",
    "module.search.enabled",
    "ui-users.create",
    "ui-users.edituserservicepoints",
    "ui-users.editproxies",
    "module.notes.enabled",
    "module.tags.enabled",
    "settings.refunds.all",
    "settings.waives.all",
    "ui-requests.all",
    "stripes-util-notes.all",
    "stripes-util-notes.edit",
    "stripes-util-notes.create",
    "stripes-util-notes.delete",
    "settings.comments.all",
    "settings.feefines.all",
    "settings.payments.all",
    "settings.transfertypes.all",
    "settings.usergroups.all",
    "settings.owners.all",
    "settings.addresstypes.all",
    "trigger.all",
    "extractor.all",
    "organizations-storage.organizations.all",
    "organizations-storage.addresses.collection.get",
    "organizations-storage.addresses.item.post",
    "organizations-storage.addresses.item.delete",
    "organizations-storage.categories.collection.get",
    "organizations-storage.categories.item.post",
    "organizations-storage.categories.item.delete",
    "organizations-storage.contacts.collection.get",
    "organizations-storage.contacts.item.post",
     "organizations-storage.contacts.item.delete",
     "organizations-storage.contacts.item.get",
     "task.item.post"
  ]
}' > tern_admin_perms.json

curl -v -X PUT -H "X-Okapi-Tenant:tern" -H "$token_header" -H "Content-Type: application/json" "https://folio-okapiq22.library.tamu.edu/perms/users/$perm_id" -d "@tern_admin_perms.json"

# cleanup
rm -rf login-headers.tmp
