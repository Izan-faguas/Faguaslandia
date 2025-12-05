<?php
header('Content-Type: application/json');

// Conexión a la BD
$conn = new mysqli("localhost", "root", "tuPassword", "faguaslandia");
if ($conn->connect_error) {
    die(json_encode(["error" => "Error de conexión"]));
}

// Recoger datos
$id = intval($_POST['id']);
$nombre = $_POST['nombre'];
$email = $_POST['email'];
$foto_subida = null;

// Subir la foto si hay archivo
if (isset($_FILES['foto']) && $_FILES['foto']['error'] == 0) {
    $nombreArchivo = uniqid() . "_" . basename($_FILES['foto']['name']);
    $ruta = "uploads/" . $nombreArchivo;

    if (move_uploaded_file($_FILES['foto']['tmp_name'], $ruta)) {
        $foto_subida = $ruta;
    } else {
        echo json_encode(["error" => "Error al subir la imagen"]);
        exit;
    }
}

// Crear query según si hay foto o no
if ($foto_subida) {
    $sql = "UPDATE usuarios SET nombre=?, email=?, avatar_url=? WHERE id=?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssi", $nombre, $email, $foto_subida, $id);
} else {
    $sql = "UPDATE usuarios SET nombre=?, email=? WHERE id=?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssi", $nombre, $email, $id);
}

if ($stmt->execute()) {
    echo json_encode(["success" => true, "foto" => $foto_subida]);
} else {
    echo json_encode(["error" => "Error al actualizar el usuario"]);
}

$stmt->close();
$conn->close();
?>
