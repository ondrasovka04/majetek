import {
  AppBar,
  Toolbar,
  Typography,
  Menu,
  MenuItem,
  Button,
  IconButton,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuthStore } from "../store/authStore";

const MenuBar = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleCloseMenu = () => setAnchorEl(null);

  return (
    <AppBar position="sticky">
      <Toolbar className="justify-between">
        <div className="flex items-center gap-4">
          <IconButton
            edge="start"
            color="inherit"
            onClick={(e) => setAnchorEl(e.currentTarget)}
          >
            <MenuIcon />
            <Typography variant="subtitle2" style={{ marginLeft: "8px", marginTop: "1px" }}>ČÍSELNÍKY</Typography>
          </IconButton>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleCloseMenu}
          >
            <MenuItem
              component={Link}
              to="/asset-group"
              onClick={handleCloseMenu}
            >
              Skupina
            </MenuItem>
            <MenuItem component={Link} to="/class" onClick={handleCloseMenu}>
              Třída
            </MenuItem>
            <MenuItem component={Link} to="/location" onClick={handleCloseMenu}>
              Umístění
            </MenuItem>
            <MenuItem
              component={Link}
              to="/purchase-method"
              onClick={handleCloseMenu}
            >
              Způsob pořízení
            </MenuItem>
            <MenuItem
              component={Link}
              to="/write-off-method"
              onClick={handleCloseMenu}
            >
              Způsob vyřazení
            </MenuItem>
          </Menu>
          <Button
            color="inherit"
            onClick={() => {
              navigate("/card");
            }}
          >
            Majetkové karty
          </Button>
          <Button
            color="inherit"
            onClick={() => {
              navigate("/report");
            }}
          >
            Sestavy
          </Button>
        </div>
        <div className="flex items-center gap-4">
          <Button color="inherit" component={Link} to="/user">
            {user?.name}
          </Button>
          <Button
            color="inherit"
            onClick={() => {
              logout();
              navigate("/");
            }}
          >
            Odhlásit se
          </Button>
        </div>
      </Toolbar>
    </AppBar>
  );
};

export default MenuBar;
