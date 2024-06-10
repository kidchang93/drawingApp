<template>
  <div>
    <DrawingComponent :room-id="roomId" :webSocketServer="webSocketServer">
      <template #background>
        <div v-html="exampleContent"></div>
      </template>
    </DrawingComponent>
  </div>
</template>

<script>
import DrawingComponent from "@/components/DrawingComponent.vue";

export default {
  components: {DrawingComponent},
  setup() {


    return {}
  }
}
</script>

<style lang="scss" scoped>

</style>