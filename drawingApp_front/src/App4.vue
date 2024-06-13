<template>
  <DrawingComponent :roomId="roomId" :webSocketServer="webSocketServer">
    <template #background>
      <div></div>
    </template>
  </DrawingComponent>
</template>

<script>
import DrawingComponent from "@/components/Whiteboard.vue";

export default {
  name: 'MainPage',
  components: {DrawingComponent},
  props: {
    roomId: {
      type: String,
      required: true
    },
    webSocketServer: {
      type: String,
      required: true
    }
  }
};
</script>

<style scoped>
</style>