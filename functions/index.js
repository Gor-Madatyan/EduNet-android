// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const {logger} = require("firebase-functions");
const {onDocumentUpdated} = require("firebase-functions/v2/firestore");

// The Firebase Admin SDK to access Firestore.
const {initializeApp} = require("firebase-admin/app");
const {FieldValue} = require("firebase-admin/firestore");

initializeApp();

/**
 * @param {*} before the previous array containing users
 * @param {*} after the new array containing users
 * @param {*} membersType the type of members the notification is related to
 * @param {*} arePending is the notification related to users that are pending
 * @return {*} new notification with base properties
 */
function getUsersNotification(before, after, membersType, arePending) {
  const notification = {};

  const longQueue = after.length > before.length ? after : before;
  const shortQueue = after.length < before.length ? after : before;

  notification.operationType =
    after.length > before.length ? "ADDED" : "REMOVED";
  notification.membersType = membersType;
  notification.arePending = arePending;
  notification.timestamp = FieldValue.serverTimestamp();

  notification.users =
    longQueue.filter((value) => !shortQueue.includes(value));

  return notification;
}

/**
 * @param {*} before the previous array containing users
 * @param {*} after the new array containing users
 * @param {*} arePending is the notification related to users that are pending
 * @return {*} new notification with base
 *             properties and membersType set to "PARTICIPANT"
 */
function getParticipantsNotification(before, after, arePending) {
  return getUsersNotification(before, after, "PARTICIPANT", arePending);
}

/**
 * @param {*} before the previous array containing users
 * @param {*} after the new array containing users
 * @param {*} arePending is the notification related to users that are pending
 * @return {*} new notification with base
 *             properties and membersType set to "ADMIN"
 */
function getAdminsNotification(before, after, arePending) {
  return getUsersNotification(before, after, "ADMIN", arePending);
}

/**
 * @param {*} before the previous array containing users
 * @param {*} after the new array containing users
 * @param {*} arePending is the notification related to users that are pending
 * @return {*} new notification with base
 *             properties and membersType set to "GRADUATED"
 */
function getGraduationsNotification(before, after) {
  return getUsersNotification(before, after, "GRADUATED", false);
}


exports.addNotification = onDocumentUpdated("/communities/{cid}", (event) => {
  const after = event.data.after.data();
  const before = event.data.before.data();
  const notificationCollection =
     event.data.after.ref.collection("notifications");
  let notification;

  if (after.graduated.length !== before.graduated.length) {
    notification =
        getGraduationsNotification(
            before.graduated,
            after.graduated);
  } else if (after.admins.length !== before.admins.length) {
    notification =
        getAdminsNotification(
            before.admins,
            after.admins,
            false);
  } else if (after.participants.length !== before.participants.length) {
    notification =
        getParticipantsNotification(
            before.participants,
            after.participants,
            false);
  } else if (after.participantsQueue.length !==
            before.participantsQueue.length) {
    notification =
        getParticipantsNotification(
            before.participantsQueue,
            after.participantsQueue,
            true);
  } else if (after.adminsQueue.length !== before.adminsQueue.length) {
    notification =
        getAdminsNotification(
            before.adminsQueue,
            after.adminsQueue,
            true);
  } else return null;

  logger.log("new notification is creating", event.params.cid);

  return notificationCollection.add(notification);
});
